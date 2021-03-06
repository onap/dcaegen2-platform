# ============LICENSE_START=======================================================
# org.onap.dcae
# ================================================================================
# Copyright (c) 2017-2018 AT&T Intellectual Property. All rights reserved.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============LICENSE_END=========================================================
#
# ECOMP is a trademark and service mark of AT&T Intellectual Property.

# -*- coding: utf-8 -*-
"""
Provides Consul helper functions
"""
import re
import json
import contextlib
from collections import defaultdict
from itertools import chain
from functools import partial
from datetime import datetime
from uuid import uuid4

import six
from copy import deepcopy
from consul import Consul

from dcae_cli.util.logger import get_logger
from dcae_cli.util.exc import DcaeException
from dcae_cli.util.profiles import get_profile
from dcae_cli.util.config import get_docker_logins_key

import os
import click

logger = get_logger('Discovery')

# NOTE: Removed the suffix completely. The useful piece of the suffix was the
# location but it was implemented in a static fashion (hardcoded). Rather than
# enhancing the existing approach and making the suffix dynamic (to support
# "rework-central" and "solutioning"), the thinking is to revisit this name stuff
# and use Consul's query interface so that location is a tag attribute.
_inst_re = re.compile(r"^(?P<user>[^.]*).(?P<hash>[^.]*).(?P<ver>\d+-\d+-\d+).(?P<comp>.*)$")


class DiscoveryError(DcaeException):
    pass

class DiscoveryNoDownstreamComponentError(DiscoveryError):
    pass


def default_consul_host():
    """Return default consul host

    This method was created to purposefully make fetching the default lazier than
    the previous impl. The previous impl had the default as a global variable and
    thus requiring the configuration to be setup before doing anything further.
    The pain point of that impl is in unit testing where now all code that
    imported this module had a strict dependency upon the impure configuration.
    """
    return get_profile().consul_host


def _choose_consul_host(consul_host):
    """Chooses the appropriate consul host

    Chooses between a provided value and a default
    """
    return default_consul_host() if consul_host == None else consul_host


def replace_dots(comp_name, reverse=False):
    '''Converts dots to dashes to prevent downstream users of Consul from exploding'''
    if not reverse:
        return comp_name.replace('.', '-')
    else:
        return comp_name.replace('-', '.')

# Utility functions for using Consul

def _is_healthy_pure(get_health_func, instance):
    """Checks to see if a component instance is running healthy

    Pure function edition

    Args
    ----
    get_health_func: func(string) -> complex object
        Look at unittests in test_discovery to see examples
    instance: (string) fully qualified name of component instance

    Returns
    -------
    True if instance has been found and is healthy else False
    """
    index, resp = get_health_func(instance)

    if resp:
        def is_passing(instance):
            return all([check["Status"] == "passing" for check in instance["Checks"]])
        return any([is_passing(instance) for instance in resp])
    else:
        return False

def is_healthy(consul_host, instance):
    """Checks to see if a component instance is running healthy

    Impure function edition

    Args
    ----
    consul_host: (string) host string of Consul
    instance: (string) fully qualified name of component instance

    Returns
    -------
    True if instance has been found and is healthy else False
    """
    cons = Consul(consul_host)
    return _is_healthy_pure(cons.health.service, instance)

def _get_instances_from_kv(get_from_kv_func, user):
    """Get component instances from kv store

    Deployed component instances get entries in a kv store to store configuration
    information. This is a way to source a list of component instances that were
    attempted to run. A component could have deployed but failed to register itself.
    The only trace of that deployment would be checking the kv store.

    Args
    ----
    get_from_kv_func: func(string, boolean) -> (don't care, list of dicts)
        Look at unittests in test_discovery to see examples
    user: (string) user id

    Returns
    -------
    List of unique component instance names
    """
    # Keys from KV contain rels key entries and non-rels key entries. Keep the
    # rels key entries but remove the ":rel" suffix because we are paranoid that
    # this could exist without the other
    _, instances_kv = get_from_kv_func(user, recurse=True)
    return [] if instances_kv is None \
            else list(set([ dd["Key"].replace(":rel", "") for dd in instances_kv ]))

def _get_instances_from_catalog(get_from_catalog_func, user):
    """Get component instances from catalog

    Fetching instances from the catalog covers the deployment cases where
    components registered successfully regardless of their health check status.

    Args
    ----
    get_from_catalog_func: func() -> (don't care, dict)
        Look at unittests in test_discovery to see examples
    user: (string) user id

    Returns
    -------
    List of unique component instance names
    """
    # Get all services and filter here by user
    response = get_from_catalog_func()
    return list(set([ instance for instance in response[1].keys() if user in instance ]))

def _merge_instances(user, *get_funcs):
    """Merge the result of an arbitrary list of get instance function calls

    Args
    ----
    user: (string) user id
    get_funcs: func(string) -> list of strings
        Functions that take in a user parameter to output a list of instance
        names

    Returns
    -------
    List of unique component instance names
    """
    return list(set(chain.from_iterable([ get_func(user) for get_func in get_funcs ])))

def _get_instances(consul_host, user):
    """Get all deployed component instances for a given user

    Sourced from multiple places to ensure we get a complete list of all
    component instances no matter what state they are in.

    Args
    ----
    consul_host: (string) host string of Consul
    user: (string) user id

    Returns
    -------
    List of unique component instance names
    """
    cons = Consul(consul_host)

    get_instances_from_kv = partial(_get_instances_from_kv, cons.kv.get)
    get_instances_from_catalog = partial(_get_instances_from_catalog, cons.catalog.services)

    return _merge_instances(user, get_instances_from_kv, get_instances_from_catalog)


# Custom (sometimes higher order) "discovery" functionality

def _make_instances_map(instances):
    """Make an instance map

    Instance map is a dict where the keys are tuples (component type, component version)
    that map to a set of strings that are instance names.
    """
    mapping = defaultdict(set)
    for instance in instances:
        match = _inst_re.match(instance)
        if match is None:
            continue

        _, _, ver, comp = match.groups()
        cname = replace_dots(comp, reverse=True)
        version = replace_dots(ver, reverse=True)
        key = (cname, version)
        mapping[key].add(instance)
    return mapping


def get_user_instances(user, consul_host=None, filter_instances_func=is_healthy):
    '''Get a user's instance map

    Args:
    -----
    filter_instances_func: fn(consul_host, instance) -> boolean
        Function used to filter instances. Default is is_healthy

    Returns:
    --------
    Dict whose keys are component (name,version) tuples and values are list of component instance names
    '''
    consul_host = _choose_consul_host(consul_host)
    filter_func = partial(filter_instances_func, consul_host)
    instances = list(filter(filter_func, _get_instances(consul_host, user)))

    return _make_instances_map(instances)


def _get_component_instances(filter_instances_func, user, cname, cver, consul_host):
    """Get component instances that are filtered

    Args:
    -----
    filter_instances_func: fn(consul_host, instance) -> boolean
        Function used to filter instances

    Returns
    -------
    List of strings where the strings are fully qualified instance names
    """
    instance_map = get_user_instances(user, consul_host=consul_host,
            filter_instances_func=filter_instances_func)

    # REVIEW: We don't restrict component names from using dashes. We do
    # transform names with dots to use dashes for domain segmenting reasons.
    # Instance map creation always reverses that making dashes to dots even though
    # the component name may have dashes. Thus always search for instances by
    # a dotted component name. We are open to a collision but that is low chance
    # - someone has to use the same name in dotted and dashed form which is weird.
    cname_dashless = replace_dots(cname, reverse=True)

    # WATCH: instances_map.get returns set. Force to be list to have consistent
    # return
    return list(instance_map.get((cname_dashless, cver), []))

def get_healthy_instances(user, cname, cver, consul_host=None):
    """Lists healthy instances of a particular component for a given user

    Returns
    -------
    List of strings where the strings are fully qualified instance names
    """
    consul_host = _choose_consul_host(consul_host)
    return _get_component_instances(is_healthy, user, cname, cver, consul_host)

def get_defective_instances(user, cname, cver, consul_host=None):
    """Lists *not* running instances of a particular component for a given user

    This means that there are component instances that are sitting out there
    deployed but not successfully running.

    Returns
    -------
    List of strings where the strings are fully qualified instance names
    """
    def is_not_healthy(consul_host, component):
        return not is_healthy(consul_host, component)

    consul_host = _choose_consul_host(consul_host)
    return _get_component_instances(is_not_healthy, user, cname, cver, consul_host)


def lookup_instance(consul_host, name):
    """Query Consul for service details"""
    cons = Consul(consul_host)
    index, results = cons.catalog.service(name)
    return results

def parse_instance_lookup(results):
    """Parse the resultset from lookup_instance

    Returns:
    --------
    String in host form <address>:<port>
    """
    if results:
        # Just grab first
        result = results[0]
        return "{address}:{port}".format(address=result["ServiceAddress"],
                port=result["ServicePort"])
    else:
        return


def _create_rels_key(config_key):
    """Create rels key from config key

    Assumes config_key is well-formed"""
    return "{:}:rel".format(config_key)


def _create_dmaap_key(config_key):
    """Create dmaap key from config key

    Assumes config_key is well-formed"""
    return "{:}:dmaap".format(config_key)


def _create_policies_key(config_key):
    """Create policies key from config key

    Assumes config_key is well-formed"""
    return "{:}:policies/".format(config_key)

def clear_user_instances(user, host=None):
    '''Removes all Consul key:value entries for a given user'''
    host = _choose_consul_host(host)
    cons = Consul(host)
    cons.kv.delete(user, recurse=True)


_multiple_compat_msg = '''Component '{cname}' config_key '{ckey}' has multiple compatible downstream \
components: {compat}. The current infrastructure can only support interacing with a single component. \
Only downstream component '{chosen}' will be connected.'''

_no_compat_msg = "Component '{cname}' config_key '{ckey}' has no compatible downstream components."

_no_inst_msg = '''Component '{cname}' config_key '{ckey}' is compatible with downstream component '{chosen}' \
however there are no instances available for connecting.'''


def _cfmt(*args):
    '''Returns a string formatted representation for a component and version'''
    if len(args) == 1:
        return ':'.join(args[0])
    elif len(args) == 2:
        return ':'.join(args)
    else:
        raise DiscoveryError('Input should be name, version or (name, version)')


def _get_downstream(cname, cver, config_key, compat_comps, instance_map,
        force=False):
    '''
    Returns a component type and its instances to use for a given config key

    Parameters
    ----------
    cname : string
        Name of the upstream component
    cver : string
        Version of the upstream component
    config_key : string
        Mainly used for populating warnings meaningfully
    compat_comps : dict
        A list of component (name, version) tuples
    instance_map : dict
        A dict whose keys are component (name, version) tuples and values are a list of instance names
    '''
    if not compat_comps:
        conn_comp = ('', '')
        logger.warning(_no_compat_msg.format(cname=_cfmt(cname, cver), ckey=config_key))
    else:
        conn_comp = six.next(iter(compat_comps))
        if len(compat_comps) > 1:
            logger.warning(_multiple_compat_msg.format(cname=_cfmt(cname, cver), ckey=config_key,
                                                       compat=list(map(_cfmt, compat_comps)), chosen=_cfmt(conn_comp)))
    if all(conn_comp):
        instances = instance_map.get(conn_comp, tuple())
        if not instances:
            if force:
                logger.warning(_no_inst_msg.format(cname=_cfmt(cname, cver), \
                        ckey=config_key, chosen=_cfmt(conn_comp)))
            else:
                logger.error(_no_inst_msg.format(cname=_cfmt(cname, cver), \
                        ckey=config_key, chosen=_cfmt(conn_comp)))
                raise DiscoveryNoDownstreamComponentError("No compatible downstream component found.")
    else:
        instances = tuple()

    return conn_comp, instances


def create_config(user, cname, cver, params, interface_map, instance_map, dmaap_map,
        instance_prefix=None, force=False):
    '''
    Creates a config and corresponding rels entries in Consul. Returns the Consul the keys and entries.

    Parameters
    ----------
    user : string
        The user namespace to create the config and rels under. E.g. user.foo.bar...
    cname : string
        Name of the upstream component
    cver : string
        Version of the upstream component
    params : dict
        Parameters of the component, taken directly from the component specification
    interface_map : dict
        A dict mapping the config_key of published streams and/or called services to a list of compatible
        component types and versions
    instance_map : dict
        A dict mapping component types and versions to a list of instances currently running
    dmaap_map : dict
        A dict that contains config key to dmaap information. This map is checked
        first before checking the instance_map which means before checking for
        direct http components.
    instance_prefix : string, optional
        The unique prefix to associate with the component instance whose config is being created
    force: string, optional
        Config will continue to be created even if there are no downstream compatible
        component when this flag is set to True. Default is False.
    '''
    inst_pref = str(uuid4()) if instance_prefix is None else instance_prefix
    conf_key = "{:}.{:}.{:}.{:}".format(user, inst_pref, replace_dots(cver), replace_dots(cname))
    rels_key = _create_rels_key(conf_key)
    dmaap_key = _create_dmaap_key(conf_key)

    conf = params.copy()
    rels = list()

    # NOTE: The dmaap_map entries are broken up between the templetized config
    # and the dmaap json in Consul
    for config_key, dmaap_goodies in six.iteritems(dmaap_map):
        conf[config_key] = deepcopy(dmaap_map[config_key])
        # Here comes the magic. << >> signifies dmaap to downstream config
        # binding service.
        conf[config_key]["dmaap_info"] = "<<{:}>>".format(config_key)

    # NOTE: The interface_map may not contain *all* possible interfaces
    # that may be connected with because the catalog.get_discovery call filters
    # based upon neighbors. Essentailly the interface_map is being pre-filtered
    # which is probably a latent bug.

    for config_key, compat_types in six.iteritems(interface_map):
        # Don't clobber config keys that have been set from above
        if config_key not in conf:
            conn_comp, instances = _get_downstream(cname, cver, config_key, \
                    compat_types, instance_map, force=force)
            conn_name, conn_ver = conn_comp
            middle = ''

            if conn_name and conn_ver:
                middle = "{:}.{:}".format(replace_dots(conn_ver), replace_dots(conn_name))
            else:
                if not force:
                    raise DiscoveryNoDownstreamComponentError("No compatible downstream component found.")

            config_val = '{{' + middle + '}}'
            conf[config_key] = config_val
            rels.extend(instances)

    dmaap_map_just_info = { config_key: v["dmaap_info"]
            for config_key, v in six.iteritems(dmaap_map) }
    return conf_key, conf, rels_key, rels, dmaap_key, dmaap_map_just_info


def get_docker_logins(host=None):
    """Get Docker logins from Consul

    Returns
    -------
    List of objects where the objects must be of the form
        {"registry": .., "username":.., "password":.. }
    """
    key = get_docker_logins_key()
    host = _choose_consul_host(host)
    (index, val) = Consul(host).kv.get(key)

    if val:
        return json.loads(val['Value'].decode("utf-8"))
    else:
        return []


def push_config(conf_key, conf, rels_key, rels, dmaap_key, dmaap_map, host=None):
    '''Uploads the config and rels to Consul'''
    host = _choose_consul_host(host)
    cons = Consul(host)
    for k, v in ((conf_key, conf), (rels_key, rels), (dmaap_key, dmaap_map)):
        cons.kv.put(k, json.dumps(v))

    logger.info("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *")
    logger.info("* If you run a 'component reconfig' command, you must first execute the following")
    logger.info("* export SERVICE_NAME={:}".format(conf_key))
    logger.info("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *")


def remove_config(config_key, host=None):
    """Deletes a config from Consul

    Returns
    -------
    True when all artifacts have been successfully deleted else False
    """
    host = _choose_consul_host(host)
    cons = Consul(host)
    #  "recurse=True" deletes the SERVICE_NAME KV and all other KVs with suffixes (:rel, :dmaap, :policies)
    results = cons.kv.delete(config_key, recurse=True)

    return results


def _group_config(config, config_key_map):
    """Groups config by streams_publishes, streams_subscribes, services_calls"""
    # Copy non streams and services first
    grouped_conf = { k: v for k,v in six.iteritems(config)
            if k not in config_key_map }

    def group(group_name):
        grouped_conf[group_name] = { k: v for k,v in six.iteritems(config)
            if k in config_key_map and config_key_map[k]["group"] == group_name }

    # Copy and group the streams and services
    # Map returns iterator so must force running its course
    list(map(group, ["streams_publishes", "streams_subscribes", "services_calls"]))
    return grouped_conf


def _apply_inputs(config, inputs_map):
    """Update configuration with inputs

    This method updates the values of the configuration parameters using values
    from the inputs map.
    """
    config.update(inputs_map)
    return config


@contextlib.contextmanager
def config_context(user, cname, cver, params, interface_map, instance_map,
        config_key_map, dmaap_map={}, inputs_map={}, instance_prefix=None,
        host=None, always_cleanup=True, force_config=False):
    '''Convenience utility for creating configs and cleaning them up

    Args
    ----
    always_cleanup: (boolean)
        This context manager will cleanup the produced config
        context always if this is True. When False, cleanup will only occur upon any
        exception getting thrown in the context manager block. Default is True.
    force: (boolean)
        Config will continue to be created even if there are no downstream compatible
        component when this flag is set to True. Default is False.
    '''
    host = _choose_consul_host(host)

    try:
        conf_key, conf, rels_key, rels, dmaap_key, dmaap_map = create_config(
                user, cname, cver, params, interface_map, instance_map, dmaap_map,
                instance_prefix, force=force_config)

        conf = _apply_inputs(conf, inputs_map)
        conf = _group_config(conf, config_key_map)

        push_config(conf_key, conf, rels_key, rels, dmaap_key, dmaap_map, host)
        yield (conf_key, conf)
    except Exception as e:
        if not always_cleanup:
            try:
                conf_key, rels_key, host
            except UnboundLocalError:
                pass
            else:
                remove_config(conf_key, host)

        raise e
    finally:
        if always_cleanup:
            try:
                conf_key, rels_key, host
            except UnboundLocalError:
                pass
            else:
                remove_config(conf_key, host)


def policy_update(policy_change_file, consul_host):

    #  Determine if it is an 'updated_policies' or 'removed_policies' change, or if user included ALL policies
    policies = True if "policies"         in policy_change_file.keys() else False
    updated  = True if "updated_policies" in policy_change_file.keys() else False
    removed  = True if "removed_policies" in policy_change_file.keys() else False

    cons          = Consul(consul_host)
    service_name  = os.environ["SERVICE_NAME"]
    policy_folder = service_name + ":policies/items/"
    event_folder  = service_name + ":policies/event"

    if policies:
        #  User specified ALL "policies" in the Policy File.  Ignore "updated_policies"/"removed_policies"
        logger.warning("The 'policies' specified in the 'policy-file' will replace all policies in Consul.")
        allPolicies = policy_change_file['policies']
        if not update_all_policies(cons, policy_folder, allPolicies):
            return False

    else:
        #  If 'removed_policies', delete the Policy from the Component KV pair
        if removed:
            policyDeletes = policy_change_file['removed_policies']
            if not remove_policies(cons, policy_folder, policyDeletes):
                return False

        #  If 'updated_policies', update the Component KV pair
        if updated:
            policyUpdates = policy_change_file['updated_policies']
            if not update_specified_policies(cons, policy_folder, policyUpdates):
                return False

    return create_policy_event(cons, event_folder, policy_folder)


def create_policy_event(cons, event_folder, policy_folder):
    """ Create a Policy 'event' KV pair in Consol """

    timestamp      = datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%S.%fZ")
    update_id      = str(uuid4())
    policies       = cons.kv.get(policy_folder, recurse=True)
    policies_count = str(policies).count("'Key':")

    event = '{"action": "gathered", "timestamp": "' + timestamp + '", "update_id": "' + update_id + '", "policies_count": ' + str(policies_count) + '}'
    if not cons.kv.put(event_folder, event):
        logger.error("Policy 'Event' creation of ({:}) in Consul failed".format(event_folder))
        return False

    return True


def update_all_policies(cons, policy_folder, allPolicies):
    """ Delete all policies from Consul, then add the policies the user specified in the 'policies' section of the policy-file """

    if not cons.kv.delete(policy_folder, recurse=True):    #  Deletes all Policies under the /policies/items folder
        logger.error("Policy delete of ({:}) in Consul failed".format(policy_folder))
        return False

    if not update_specified_policies(cons, policy_folder, allPolicies):
        return False

    return True

def update_specified_policies(cons, policy_folder, policyUpdates):
    """ Replace the policies the user specified in the 'updated_policies' (or 'policies') section of the policy-file """

    for policy in policyUpdates:
        policy_folder_id = extract_policy_id(policy_folder, policy)
        if policy_folder_id:
            policyBody = json.dumps(policy)
            if not cons.kv.put(policy_folder_id, policyBody):
                logger.error("Policy update of ({:}) in Consul failed".format(policy_folder_id))
                return False
        else:
            return False

    return True


def remove_policies(cons, policy_folder, policyDeletes):
    """ Delete the policies that the user specified in the 'removed_policies' section of the policy-file """

    for policy in policyDeletes:
        policy_folder_id = extract_policy_id(policy_folder, policy)
        if policy_folder_id:
            if not cons.kv.delete(policy_folder_id):
                logger.error("Policy delete of ({:}) in Consul failed".format(policy_folder_id))
                return False
        else:
            return False

    return True

def extract_policy_id(policy_folder, policy):
    """ Extract the Policy ID from the policyName.
        Return the Consul key (Policy Folder with Policy ID) """

    policyId_re = re.compile(r"(.*)\.\d+\.[a-zA-Z]+$")

    policyName = policy['policyName']  #  Extract the policy Id "Consul Key" from the policy name
    match      = policyId_re.match(policyName)

    if match:
        policy_id        = match.group(1)
        policy_folder_id = policy_folder + policy_id

        return policy_folder_id
    else:
        logger.error("policyName ({:}) needs to end in '.#.xml' in order to extract the Policy ID".format(policyName))
        return


def build_policy_command(policy_reconfig_path, policy_change_file, consul_host):
        """ Build command to execute the Policy Reconfig script in the Docker container """

        #  Determine if it is an 'updated_policies' and/or 'removed_policies' change, or if user included ALL policies
        all_policies = True if "policies"         in policy_change_file.keys() else False
        updated      = True if "updated_policies" in policy_change_file.keys() else False
        removed      = True if "removed_policies" in policy_change_file.keys() else False

        #  Create the Reconfig Script command (3 parts: Command and 2 ARGs)
        command = []
        command.append(policy_reconfig_path)
        command.append("policies")

        #  Create a Dictionary of 'updated', 'removed', and 'ALL' policies

        #  'updated' policies - policies come from the --policy-file
        if updated:
            updated_policies = policy_change_file['updated_policies']
        else: updated_policies = []

        policies = {}
        policies["updated_policies"] = updated_policies

        #  'removed' policies - policies come from the --policy-file
        if removed:
            removed_policies = policy_change_file['removed_policies']
        else: removed_policies = []

        policies["removed_policies"] = removed_policies

        #  ALL 'policies' - policies come from Consul
        cons          = Consul(consul_host)
        service_name  = os.environ["SERVICE_NAME"]
        policy_folder = service_name + ":policies/items/"

        id, consul_policies = cons.kv.get(policy_folder, recurse=True)

        policy_values = []
        if consul_policies:
            for policy in consul_policies:
                policy_value = json.loads(policy['Value'])
                policy_values.append(policy_value)

        policies["policies"] = policy_values

        #  Add the policies to the Docker "command" as a JSON string
        command.append(json.dumps(policies))

        return command
