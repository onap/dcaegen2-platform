/* 
 *  # ============LICENSE_START=======================================================
 *  # Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
 *  # ================================================================================
 *  # Licensed under the Apache License, Version 2.0 (the "License");
 *  # you may not use this file except in compliance with the License.
 *  # You may obtain a copy of the License at
 *  #
 *  #      http://www.apache.org/licenses/LICENSE-2.0
 *  #
 *  # Unless required by applicable law or agreed to in writing, software
 *  # distributed under the License is distributed on an "AS IS" BASIS,
 *  # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  # See the License for the specific language governing permissions and
 *  # limitations under the License.
 *  # ============LICENSE_END=========================================================
 */

import { Injectable } from '@angular/core';
import * as saveAs from 'file-saver';

@Injectable({
  providedIn: 'root'
})
export class DownloadService {

  constructor() { }

  /* * * * Download json file * * * */
  downloadJSON(content, fileName){
    let file = new Blob([JSON.stringify(content)], { type: 'text;charset=utf-8' });
    let name: string = `${fileName}.json`
    saveAs(file, name)
  }

  /* * * * Export ms instance table to excel or csv * * * */
  exportTableData(exportTo, downloadElements, arrHeader) {
    if (exportTo === "excel") {
      import("xlsx").then(xlsx => {
        const worksheet = xlsx.utils.json_to_sheet(downloadElements);
        const workbook = { Sheets: { 'data': worksheet }, SheetNames: ['data'] };
        const excelBuffer: any = xlsx.write(workbook, { bookType: 'xlsx', type: 'array' });
        this.saveAsExcelFile(excelBuffer, "Table_Data");
      });
    } else if (exportTo === "csv") {
      let csvData = this.convertToCSV(downloadElements, arrHeader)
      var blob = new Blob([csvData], { type: 'text/csv' })
      saveAs(blob, "Table_Data.csv");
    }
  }
  saveAsExcelFile(buffer: any, fileName: string): void {
    import("file-saver").then(FileSaver => {
      let EXCEL_TYPE = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8';
      let EXCEL_EXTENSION = '.xlsx';
      const data: Blob = new Blob([buffer], {
        type: EXCEL_TYPE
      });
      FileSaver.saveAs(data, fileName + '_export_' + new Date().getTime() + EXCEL_EXTENSION);
    });
  }
  convertToCSV(objArray, headerList) {
    let array = typeof objArray != 'object' ? JSON.parse(objArray) : objArray;
    let str = '';
    let row = '';
    for (let index in headerList) {
      row += headerList[index] + ',';
    }
    row = row.slice(0, -1);
    str += row + '\r\n';
    for (let i = 0; i < array.length; i++) {
      let line = '';
      for (let index in headerList) {
        let head = headerList[index];
        if (array[i][head] === null || array[i][head] === undefined) {
          line += ','
        } else {
          if (head === "Labels" && array[i][head].length > 1) { line += '[' + array[i][head].join('] [') + '],'; }
          else { line += array[i][head] + ','; }
        }
      }
      str += line + '\r\n';
    }
    return str;
  }
}
