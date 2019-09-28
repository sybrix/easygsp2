<div>
    <style>
    html {
        font-family: arial, verdana;
        font-size: .90em
    }

    .content {
        width: 680px;
        margin: 10px auto;
    }

    #stackTrace {
        color: firebrick;
        padding: 5px 5px 10px 20px;
        overflow-y: auto;
        height: 300px;
        margin: 10px auto 0 auto;
        width: 655px;
        border: 1px solid #E0E0E0;
    }

    #source {
        color: black;

        overflow-y: auto;
        height: 300px;
        margin: 0 auto;
        width: 680px;
        border: 1px solid #E0E0E0;
    }

    #lineNumber {
        margin: 10px auto 0px auto;
        width: 680px;
        font-size: .8em;

    }

    #exception {
        margin: 0px auto;
        width: 680px;
        background-color: #ffffcc;
        color: firebrick;
        font-weight: bold;
        padding: 5px 0px 5px 10px;
    }

    .sourceLineNumber {
        width: 30px;
        background-color: #ECECEC;
        border-right: 2px solid #66cc00;
        border-bottom: 1px solid #ECECEC;
        float: left;
    }

    .sourceCode {
        padding-left: 10px;
        border-bottom: 1px solid #ECECEC;
        width: 620px;
        margin-left: 5px
    }

    .sourceLine {
    }

    #errorCode {
        font-weight: bold;
    }

    #errorMessage {
        font-style: italic;
        padding-left: 20px
    }

    .path {
        font-weight: bold;
    }
    </style>

    <div class="content"><span
            id="errorCode">${requestError.errorCode} - Internal Service Error</span>. An unexpected error occurred:
        <span id="errorMessage">${requestError.errorMessage}</span></div>

    <div id="exception">
        <%=requestError.exceptionName%>
    </div>

    <div id="lineNumber">
        <%=requestError.lineNumberMessage%>
    </div>

    <% if (requestError.source != null && !requestError.source.isEmpty()) { %>
    <div class="border" id="source">
        <%=requestError.source%>
    </div>
    <% } %>

    <div class="border" id="stackTrace">
        <%=requestError.stackTraceString%>
    </div>

</div>