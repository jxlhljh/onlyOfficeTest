import React from 'react';
import { DocumentEditor } from "@onlyoffice/document-editor-react";
import './index.less';

//https://api.onlyoffice.com/zh/editors/callback
//https://api.onlyoffice.com/editors/config/
const OnlyOfficeEditor = () => {

    const onDocumentReady = (event) => {
        // 请输入函数体代码
        console.log("Document is loaded", event);
    };

    return (
        <div className='docx-editor-wrapper'>

            <DocumentEditor
                id="docxEditor"
                documentServerUrl="http://192.168.56.101:8088"
                config={{
                    "document": {
                        "fileType": "docx",
                        "key": "Khirz6zTPdf2daz",
                        "title": "Example Document Title.docx",
                        "url": "http://192.168.56.1:8888/getFile",
                        "permissions": {
                            "comment": true,
                            "copy": true,
                            "download": true,
                            "edit": true,
                            "print": true,
                            "fillForms": true,
                            "modifyFilter": true,
                            "modifyContentControl": true,
                            "review": true,
                            "commentGroups": {}
                        }
                    },
                    "documentType": "word",
                    //"type": "mobile",
                    "editorConfig": {
                        "mode": "edit",
                        "lang": "zh-CN", //中文还是英文
                        "callbackUrl": "http://192.168.56.1:8888/callback",
                        "user": {
                            "id": "liu",
                            "name": "liu"
                        },
                    },
                }}
                events_onDocumentReady={onDocumentReady}
            />
        </div>
    );
};

export default OnlyOfficeEditor;