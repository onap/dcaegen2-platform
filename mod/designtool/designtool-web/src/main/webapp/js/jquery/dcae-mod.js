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

    /**
    * @desc: on load of page, makes submit button disabled. Also makes an api call to get the host IP of the current instance
    */
    $(document).ready(function (){
        if(dt_id == null){   $('#operate-submit-btn').prop('disabled', true);   }

        getDistributionTargets();
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
                   url:  '/distributor/distribution-targets',
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
                    url:  '/distributor/distribution-targets/'+dt_id+'/process-groups',
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


function uecvalue(n) {
  return encodeURIComponent("" + $(n).val());
}

function esc(s) {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function onBoard() {
  $("#onboarding-in-progress").show();
  var url = "/acumos-adapter/onboard.js?acumos=" + uecvalue("#furl");
  if ($("#cat-menu").val() != "*") {
    url += "&catalogId=" + uecvalue("#cat-menu");
    if ($("#sol-menu").val() != "*") {
      url += "&solutionId=" + uecvalue("#sol-menu");
      if ($("#rev-menu").val() != "*") {
        url += "&revisionId=" + uecvalue("#rev-menu");
      }
    }
  }
  var xhr = new XMLHttpRequest();
  xhr.onerror = function() {
    $("#onboarding-in-progress").hide();
    alert("Onboarding failed");
  }
  xhr.onload = function() {
    if (this.status < 400) {
      $("#onboarding-in-progress").hide();
      alert("Onboarding successful");
    } else {
      alert("Onboarding error: " + this.statusText);
    }
  }
  xhr.open("POST", url);
  xhr.send();
}

function chooseSolution() {
  if ($("#sol-menu").val() == "*") {
    _updatevis();
  } else {
    lookupItem("#ac-revs", "#rev-menu", "/acumos-adapter/listRevisions.js?acumos=" + uecvalue("#furl") + "&solutionId=" + uecvalue("#sol-menu"));
  }
}

function chooseCatalog() {
  if ($("#cat-menu").val() == "*") {
    _updatevis();
  } else {
    lookupItem("#ac-sols", "#sol-menu", "/acumos-adapter/listSolutions.js?acumos=" + uecvalue("#furl") + "&catalogId=" + uecvalue("#cat-menu"));
  }
}

function lookupCatalogs() {
  $("#onboard").show();
  lookupItem("#c-acumos", "#cat-menu", "/acumos-adapter/listCatalogs.js?acumos=" + uecvalue("#furl"));
}

function lookupItem(dblock, smenu, url) {
  var xhr = new XMLHttpRequest();
  var xmenu = $(smenu);
  xmenu[0].options.length = 1;
  xmenu.val("*");
  xhr.onerror = function() {
    alert("Error querying remote Acumos system");
    $(dblock).hide();
  }
  xhr.onload = function() {
    var xresp = JSON.parse(this.response);
    var i;
    for (i = 0; i < xresp.length; i++) {
      var option = document.createElement("option");
      option.text = esc(xresp[i].name);
      option.value = xresp[i].id;
      xmenu[0].add(option);
    }
    if (xresp.length == 0) {
      $(dblock).hide();
    } else {
      $(dblock).show();
    }
    _updatevis();
  };
  xhr.open("GET", url);
  xhr.send();
}

function setModelType() {
  if ($("#model-type").val() == "mtAcumos") {
    $("#furl").val("");
    $("#mt-acumos").show();
  }
  _updatevis();
}

function _updatevis() {
  if ($("#model-type").val() != "mtAcumos") {
    $("#mt-acumos").hide();
    $("#furl").val("");
  }
  if ($("#furl").val() == "") {
    $("#c-acumos").hide();
    $("#onboard").hide();
    $("#cat-menu").val("*");
  }
  if ($("#cat-menu").val() == "*") {
    $("#ac-sols").hide();
    $("#sol-menu").val("");
  }
  if ($("#sol-menu").val() == "*") {
    $("#ac-revs").hide();
    $("#rev-menu").val("");
  }
}

function onBoardComponent() {
  _onBoardFile("#cspec", "/onboarding/components");
}

function onBoardDataFormat() {
  _onBoardFile("#dfspec", "/onboarding/dataformats");
}

function _onBoardFile(source, url) {
  reader = new FileReader();
  reader.onerror = function() {
    alert("Error reading file");
  }
  reader.onload = function(evt) {
    xhr = new XMLHttpRequest();
    xhr.onload = function() {
      if (this.status >= 400) {
        alert("File upload failed " + this.statusText);
      } else {
        alert("File upload complete");
      }
    }
    xhr.onerror = function() {
      alert("File upload failed");
    }
    xhr.open("POST", url);
    xhr.overrideMimeType("application/json");
    xhr.send(evt.target.result);
  };
  reader.readAsBinaryString($(source)[0].files[0]);
}
