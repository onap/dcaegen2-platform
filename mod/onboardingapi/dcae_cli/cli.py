# ============LICENSE_START=======================================================
# org.onap.dcae
# ================================================================================
# Copyright (c) 2017-2020 AT&T Intellectual Property. All rights reserved.
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
Provides entry-level logic for building the CLI. Commands and heavy-lifting logic should be in their own module.
"""
import click

from dcae_cli import util
from dcae_cli.commands.catalog import catalog
from dcae_cli.commands.component import component
from dcae_cli.commands.data_format import data_format
from dcae_cli.commands.profiles import profiles
from dcae_cli.catalog import get_catalog
from dcae_cli.util.exc import DcaeException
from dcae_cli.util.logger import get_logger
from dcae_cli.util import config as conf
from dcae_cli.util import profiles as prof


log = get_logger('cli')


def _reinit_cli():
    """Reinit cli"""
    click.echo("Warning! Reinitializing your dcae-cli configuration")
    try:
        conf.reinit_config()
        prof.reinit_profiles()
    except Exception as e:
        raise DcaeException("Failed to reinitialize configuration: {0}".format(e))

def _reinit_callback(ctx, param, value):
    """Callback used for the eager --reinit option"""
    if not value or ctx.resilient_parsing:
        return
    _reinit_cli()
    click.echo("Reinitialize done")
    ctx.exit()



@click.group()
@click.option('--verbose', '-v', is_flag=True, default=False, help='Prints INFO-level logs to screen.')
# This is following the same pattern as --version
# http://click.pocoo.org/5/options/#callbacks-and-eager-options
@click.option('--reinit', is_flag=True, callback=_reinit_callback, expose_value=False,
        is_eager=True, help='Re-initialize dcae-cli configuration')
@click.version_option()
@click.pass_context
def cli(ctx, verbose):

    if ctx.obj is None:
        ctx.obj = dict()

    if 'config' not in ctx.obj:
        config = conf.get_config()

        ctx.obj['config'] = config
    else:
        config = ctx.obj['config']

    if 'catalog' not in ctx.obj:
        try:
            ctx.obj['catalog'] = get_catalog(**config)
        except Exception as e:
            log.error(e)
            raise DcaeException("Having issues connecting to the onboarding catalog")

    if verbose:
        util.logger.set_verbose()


@cli.command(name="http", help="Run HTTP API")
@click.option('--live', is_flag=True, default=False, help='Starts up the HTTP API in live mode which means it binds to 80')
@click.pass_context
def run_http_api(ctx, live):
    catalog = ctx.obj['catalog']
    should_debug = not live
    # Importing http module has to be here otherwise unit tests will break
    # because http module makes config calls when the module is loaded (global).
    # Config calls must always be done lazily as much as possible in order for the
    # mock_cli_config pytest.fixture to kick in.
    from dcae_cli import http
    http.start_http_server(catalog, debug=should_debug)



cli.add_command(catalog)
cli.add_command(component)
cli.add_command(data_format)
cli.add_command(profiles)
