<!doctype html>
<html>
<meta name="viewport" content="width=device-width, initial-scale=1.0 user-scalable=no"/>
<head>
    <title>
        项目${project.name}集成测试报告
    </title>
    <style>
    a {
        text-decoration: none;
    }

    .container {
        display: flex;
        justify-content: space-between;
        margin-top: 5px;
        margin-bottom: 5px;
        background-color: #f6f9e7;
        max-width: 320px;
        border-radius: 10px;
        color: #7c7e74;
    }

    .container:hover {
        background-color: #d9ddc8;
    }

    .project_name {
        color: #7c7e74;
        overflow: hidden;
        text-overflow: ellipsis;
        max-width: 240px;
        text-align: left;
        padding: 1px 7px 1px 7px;
    }

    .status-wrapper {
        display: inline-flex;
        justify-content: flex-end;
        padding-right: 5px;
        width: 90px;
        align-items: center;
    }

    .success_icon {
        width: 10px;
        height: 10px;
        border-radius: 10px;
        background-color: #9be252;
    }

    .failed_icon {
        width: 10px;
        height: 10px;
        border-radius: 10px;
        background-color: #ff7b7b;
    }
    </style>
</head>

<body>
<g:each in="${project.list}" var="p">
    <a href="${p.reportUrl}">
        <div class="container">
            <div class="project_name">${p.name}</div>

            <div class="status-wrapper">
                <g:if test="${p.success}">
                    <span class="success_icon"></span>
                </g:if>
                <g:else>
                    <span class="failed_icon"></span>
                </g:else>
            </div>
        </div>
    </a>
</g:each>
</body>
</html>