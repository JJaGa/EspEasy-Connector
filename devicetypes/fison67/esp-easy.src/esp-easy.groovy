/**
 *  ESP Easy DTH (v.0.0.1)
 *
 *  Authors
 *   - fison67@nate.com
 *  Copyright 2018
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 
import groovy.json.JsonSlurper

metadata {
	definition (name: "ESP Easy", namespace: "fison67", author: "fison67") {
        capability "Sensor"
        
		attribute "value1", "number"
		attribute "value2", "number"
		attribute "value3", "number"
		attribute "value4", "number"
        attribute "lastCheckinDate", "date"
        
        command "setData"
        command "refresh"
        command "timerLoop"
        command "setEspName"
	}


	simulator {
	}

	tiles(scale: 2) {
    	standardTile("status1_name", "device.status1_name", width: 3, height: 1) {
            state "val", label: '${currentValue}',  backgroundColor: "#ffffff"
        }
        standardTile("status2_name", "device.status2_name", width: 3, height: 1) {
            state "val", label: '${currentValue}',  backgroundColor: "#ffffff"
        }
    	valueTile("status1", "device.status1", width: 3, height: 3) {
            state("val", label:'${currentValue}', defaultState: true, 
            	backgroundColors:[
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
                ]
            )
        }
        valueTile("status2", "device.status2", width: 3, height: 3) {
            state("val", label:'${currentValue}', defaultState: true, 
            	backgroundColors:[
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
                ]
            )
        }
        standardTile("status3_name", "device.status3_name", width: 3, height: 1) {
            state "name", label: '${currentValue}',  backgroundColor: "#ffffff"
        }
        standardTile("status4_name", "device.status4_name", width: 3, height: 1) {
            state "name", label: '${currentValue}',  backgroundColor: "#ffffff"
        }
    	valueTile("status3", "device.status3", width: 3, height: 3) {
            state("val", label:'${currentValue}', defaultState: true, 
            	backgroundColors:[
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
                ]
            )
        }
        valueTile("status4", "device.status4", width: 3, height: 3) {
            state("val", label:'${currentValue}', defaultState: true, 
            	backgroundColors:[
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
                ]
            )
        }
       
       	main (["status1","status2"])
      	details(["status1_name","status2_name","status1","status2","status3_name","status4_name","status3","status4"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def setUrl(String url){
    log.debug "URL >> ${url}"
	state.address = url
    state.lastTime = new Date().getTime()
    
    state.timeSecond = 5
    timerLoop()
}

def setEspName(name){
	log.debug "SetName >> ${name}"
	state._name = name
}

def setData(data){
	log.debug "SetData >> ${state._name}"
 //   log.debug "DATA >> ${data}"
	state._data = data
    
    try{
    
    	def count = 1
        data.each{item->
        //	log.debug item
            
            if(item.TaskName == state._name){
            	item.each{ key,value->
                    if(key == "TaskValues"){
                        value.each{ obj ->
                            obj.each{ subKey, subValue ->
                     //       	log.debug subKey + " >> " + subValue
                                if(subKey == "Name"){
                                    sendEvent(name: "status${count}_name", value: subValue)
                                }else if(subKey == "Value"){
                                    sendEvent(name: "status${count}", value: subValue)
                                }
                            }
                            count += 1
                        }
                    }
                }
            }
        }

        def now = new Date()
        state.lastTime = now.getTime()
        sendEvent(name: "lastCheckinDate", value: now)
    }catch(e){
    	log.error "Error!!! ${e}"
    }
}


def timerLoop(){
	getStatusOfESPEasy()    
	startTimer(state.timeSecond.toInteger(), timerLoop)
}

def startTimer(seconds, function) {
    def now = new Date()
	def runTime = new Date(now.getTime() + (seconds * 1000))
	runOnce(runTime, function) // runIn isn't reliable, use runOnce instead
}

def callback(physicalgraph.device.HubResponse hubResponse){
	def msg, json, status
    try {
        msg = parseLanMessage(hubResponse.description)
        def jsonObj = msg.json
        setData(jsonObj.Sensors)
    } catch (e) {
        log.error "Exception caught while parsing data: " + e 
    }
}

def getStatusOfESPEasy() {
    try{
    	def timeGap = new Date().getTime() - Long.valueOf(state.lastTime)
        if(timeGap > 1000 * 60){
            log.warn "ESP Easy device is not connected..."
            sendEvent(name: "status1", value: -1)
            sendEvent(name: "status2", value: -1)
            sendEvent(name: "status3", value: -1)
            sendEvent(name: "status4", value: -1)
        }
		log.debug "Try to get data from ${state.address}"
        def options = [
            "method": "GET",
            "path": "/json",
            "headers": [
                "HOST": state.address + ":80",
                "Content-Type": "application/json"
            ]
        ]
        def myhubAction = new physicalgraph.device.HubAction(options, null, [callback: callback])
        sendHubCommand(myhubAction)
    }catch(e){
    	log.error "Error!!! ${e}"
    }
}
