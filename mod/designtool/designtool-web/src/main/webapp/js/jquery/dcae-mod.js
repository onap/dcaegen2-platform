/*
============LICENSE_START=======================================================
Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
================================================================================
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
============LICENSE_END=========================================================
*/

console.log("loading dcae-mod");

    var dt_id;
    var hostname;

    /**
    * @desc: on load of page, makes submit button disabled. Also makes an api call to get the host IP of the current instance
    */
    $(document).ready(function (){
        if(dt_id == null){   $('#operate-submit-btn').prop('disabled', true);   }

        //get hostname
        $.ajax({
               type: 'GET',
               url:   '../nifi-api/flow/config',
               dataType: 'json',
               contentType: 'application/json',
               success: function(data){
                    hostname= data.flowConfiguration.dcaeDistributorApiHostname;

                   //function call: invokes api to refresh the list of Envs
                    if(hostname){    getDistributionTargets();   }
                  }
          });
    });

   /**
    * common function to reuse : invokes api to get new updates list environments.
    * @desc: Makes the select dropdown empty first. Then manually add Placeholder as first/default Option.
    *        And then dynamically add list of Environments as Options.
    */
    function getDistributionTargets(){
        var select = document.getElementById("environmentType");
         if(select && select.options && select.options.length > 0){
             select.options.length=0;
             var element= document.createElement("option");
             element.textContent= "Select Environment";
             element.selected=true;
             element.disabled=true;
             element.className="combo-option-text";
             select.appendChild(element);
          }else{  select=[];   }

           $.ajax({
                   type: 'GET',
                   url:  hostname+'/distribution-targets',
                   dataType: 'json',
                   contentType: 'application/json',
                   success: function(data){
                    if(data){
                           for(var i=0; i < data.distributionTargets.length; i++){
                             var opt= data.distributionTargets[i];
                             var element= document.createElement("option");
                              element.textContent= opt.name;
                              element.value= opt.id;
                              element.className="combo-option-text";
                              select.appendChild(element);
                           }
                    }
                  }
           })
    }

    /**
    * @desc: submit button functionality to distribute/POST process group to the environment.
    */
     var distributeGraph = function(){
        var selected_id = $('#operation-context-id').text();
        // process group id (nifi api) != flow id (nifi registry api)
        // so must first fetch the flow id from nifi api
        $.ajax({
          type: 'GET',
          url: '../nifi-api/process-groups/'+selected_id,
          contentType: 'application/json',
          success: function(data) {
            const flow_id = data["component"]["versionControlInformation"]["flowId"];
            const request = {"processGroupId": flow_id}

            $.ajax({
                    type: 'POST',
                    data: JSON.stringify(request),
                    url:  hostname+'/distribution-targets/'+dt_id+'/process-groups',
                    dataType: 'json',
                    contentType: 'application/json',
                    success: function(data){
                         alert("Success, Your flow have been distributed successfully");
                    },
                    error: function(err) {
                        alert("Issue with distribution:\n\n" + JSON.stringify(err, null, 2));
                    }
            });
          }
        })
     };


   /**
   * @desc: selection of distribution target environment to post the process group
   */
   var onEnvironmentSelect = function(){
     dt_id = $('#environmentType').val();
     console.log(dt_id);
     if(dt_id == null){   $('#operate-submit-btn').prop('disabled', true);   }
     else{  $('#operate-submit-btn').prop('disabled', false);      }
    };


    /**
    * @desc: event handler for Refresh icon in Operate panel :  invokes api to refresh the list of Envs
    */
    var refreshEnvironments= function(){    getDistributionTargets();    };


    /**
    * @desc: event handler for Close icon of Setting/ Distribution Env CRUD dialog :  invokes api to refresh the list of Envs
    */
    var onCloseSettings= function(){  getDistributionTargets();  };
