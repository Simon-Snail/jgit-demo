package com.example.jgit;

import com.example.jgit.api.JGitApi;
import com.example.jgit.commons.CloneRepositoryParam;
import com.example.jgit.commons.PublishParam;

public class JGitTest {

    public static void main(String[] args) throws Exception {

        String localPath = "/Users/xxxxx/volans/jgit";
        String remoteUrl = "https://gitlab.com/xxxxx/testapi.git";
        String username = "xxx";
        String password = "xxx";

        //所需参数
        CloneRepositoryParam param = new CloneRepositoryParam();
        param.setLocalPath(localPath);
        param.setRemoteUrl(remoteUrl);
        param.setUsername(username);
        param.setPassword(password);

        //下载远程项目到本地
        JGitApi.cloneRemoteRepository(param);

        //推送本地代码到远程
        JGitApi.pushAll(localPath, username,password);

        //分支合并并推送到远程
        PublishParam publishParam = new PublishParam();
        publishParam.setLocalPath(localPath);
        publishParam.setUsername(username);
        publishParam.setPassword(password);
        publishParam.setBranchFrom("dev");
        publishParam.setBranchTo("master");
        JGitApi.merageAndPush(publishParam);


    }

}
