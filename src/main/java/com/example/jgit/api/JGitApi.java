package com.example.jgit.api;

import com.example.jgit.commons.CloneRepositoryParam;
import com.example.jgit.commons.PublishParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * @author Simon
 */
@Component
public class JGitApi {
    private static Logger logger = LogManager.getLogger(JGitApi.class);

    //默认clone分支
    private static final String DEFAULT_BRANCH = "release";

    private static final String MESSAGE = "PUSH";

    /**
     * git clone
     * @param param
     * @throws Exception
     */
    public static void cloneRemoteRepository(CloneRepositoryParam param) throws Exception {
        CloneCommand cloneCommand = Git.cloneRepository();
        //远程路径，并指定下载的分支
        cloneCommand.setURI(param.getRemoteUrl()).setBranch(DEFAULT_BRANCH);
        //需要git用户名密码
        cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(param.getUsername(), param.getPassword()));
        //本地下载的路径
        cloneCommand.setDirectory(new File(param.getLocalPath()));
        logger.info("remote:" + param.getRemoteUrl());
        logger.info("branchName:" + DEFAULT_BRANCH);
        Git result = cloneCommand.call();

        logger.info("Having repository: " + result.getRepository().getDirectory());
    }

    /**
     * 推送代码到远程
     * @param localPath 本地文件路径
     */
    public static void pushAll(String localPath, String username, String password) {
        try {
            Git git = Git.open(new File(localPath));

            git.add().addFilepattern(".").call();

            git.commit().setMessage(MESSAGE).call();

            PushCommand pcmd = git.push();
            pcmd.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
            pcmd.call();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[git commit/push failed!]" + e);
            throw new RuntimeException("[git commit/push failed!]" + e);
        }
    }

    /**
     * 分支合并并推送到远程
     * @param param
     */
    public static void merageAndPush(PublishParam param) {
        try {
            //打开
            Git git = Git.open(new File(param.getLocalPath()));

            //check branch是否存在并拉去最新
            checkoutAndPull(git, param.getBranchTo());
            Ref check = checkoutAndPull(git, param.getBranchFrom());

            //命令是api模块的一部分，其中包括类似git的调用
            CheckoutCommand coCmd = git.checkout();
            //merage to BranchTo
            coCmd.setName(param.getBranchTo());
            coCmd.setCreateBranch(false);
            //切换到分支
            coCmd.call();

            MergeCommand mgCmd = git.merge();
            mgCmd.include(check);
            //合并
            MergeResult res = mgCmd.call();
            if(res.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING)){
                //告知用户他必须处理冲突
                System.out.println(res.getConflicts().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[git merage!]" + e);
            throw new RuntimeException("[git merage!]" + e);
        }

        pushAll(param.getLocalPath(), param.getUsername(), param.getPassword());
    }

    /**
     * 检查本地分支是否存在
     * @param git
     * @param branchName
     * @return
     * @throws GitAPIException
     */
    public static boolean branchNameExist(Git git, String branchName) throws GitAPIException {
        List<Ref> refs = git.branchList().call();
        for (Ref ref : refs) {
            if (ref.getName().equals("refs/heads/" + branchName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * checkout 并拉取最新
     * @param git
     * @return
     * @throws Exception
     */
    public static Ref checkoutAndPull(Git git, String branchName) {
        Ref ref;
        try {
            if (branchNameExist(git, branchName)) {
                //如果分支在本地已存在，直接checkout即可。
                ref = git.checkout().setCreateBranch(false).setName(branchName).call();
            } else {
                //如果分支在本地不存在，需要创建这个分支，并追踪到远程分支上面。
                ref = git.checkout().setCreateBranch(true).setName(branchName).setStartPoint("refs/remotes/origin/" + branchName).call();
            }
            //拉取最新的提交
            git.pull().call();
            return ref;
        } catch (GitAPIException e) {
            e.printStackTrace();
            logger.error("[git pull!]" + e);
            throw new RuntimeException("[git pull!]" + e);
        } finally {
            git.close();
        }
    }

}
