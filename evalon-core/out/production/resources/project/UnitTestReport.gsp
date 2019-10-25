<!doctype html>
<html>
<meta name="viewport" content="width=device-width, initial-scale=1.0 user-scalable=no"/>
<head>
    <title>
        项目${project.name}单元测试报告
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
        justify-content: flex-start;
        width: 90px;
        align-items: center;
    }

    .success_icon {
        width: 10px;
        height: 10px;
        border-radius: 10px;
        background-color: #9be252;
    }

    .success {
        color: #7c7e74;
        padding: 1px 7px 1px 7px;
    }

    .skipped_icon {
        width: 10px;
        height: 10px;
        border-radius: 10px;
        background-color: #ffed67;
    }

    .skipped {
        color: #7c7e74;
        padding: 1px 7px 1px 7px;
    }

    .failed_icon {
        width: 10px;
        height: 10px;
        border-radius: 10px;
        background-color: #ff7b7b;
    }

    .failed {
        color: #7c7e74;
        padding: 1px 7px 1px 7px;
    }
    </style>
</head>

<body>
<g:each in="${project.list}" var="p">
    <g:if test="${p.success == 0 && p.skipped == 0 && p.failed == 0}">
        <a href="#">
    </g:if>
    <g:else>
        <a href="http://adcz-dev-napos-team-4.vm.elenet.me/testreport/${project.sha}/${p.name}">
    </g:else>
    <div class="container">
        <div class="project_name">${p.name}</div>

        <div class="status-wrapper">
            <span class="success_icon"></span>

            <span class="success">${p.success}</span>

            <span class="skipped_icon"></span>

            <span class="skipped">${p.skipped}</span>

            <span class="failed_icon"></span>

            <span class="failed">${p.failed}</span>
        </div>
    </div>
    </a>
</g:each>
</body>
</html>