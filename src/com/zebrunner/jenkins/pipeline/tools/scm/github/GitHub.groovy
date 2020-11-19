package com.zebrunner.jenkins.pipeline.tools.scm.github

import com.zebrunner.jenkins.pipeline.tools.scm.Scm
import com.zebrunner.jenkins.pipeline.Configuration

import static com.zebrunner.jenkins.pipeline.Executor.*

class GitHub extends Scm {

    GitHub(context) {
        super(context)
        
        this.prRefSpec = '+refs/pull/*:refs/remotes/origin/pr/*'
        this.branchSpec = "origin/pr/%s/merge"
    }

    enum HookArgs {
        GIT_TYPE("scmType", "github"),
        SSH_RUL("sshUrl", "\$.repository.ssh_url"),
        HTTP_URL("httpUrl", "\$.repository.clone_url"),
        HEADER_EVENT_NAME("eventName", "x-github-event"),

        PR_ACTION("prAction", "\$.action"),
        PR_SHA("prSha", "\$.pull_request.head.sha"),
        PR_NUMBER("prNumber", "\$.number"),
        PR_REPO("prRepo", "\$.pull_request.base.repo.full_name"),
        PR_SOURCE_BRANCH("prSourceBranch", "\$.pull_request.head.ref"),
        PR_TARGET_BRANCH("prTargetBranch", "\$.pull_request.base.ref"),
        PR_FILTER_TEXT("prFilterText", "\$pr_action \$x_github_event %s"),
        PR_FILTER_REGEX("prFilterExpression", "^((opened|reopened|synchronize)\\spull_request\\s%s)*?\$"),

        
        PUSH_FILTER_TEXT("pushFilterText", "\$ref \$x_github_event %s"),
        PUSH_FILTER_REGEX("pushFilterExpression", "^(refs/heads/master\\spush\\s%s)*?\$"),
        REF_JSON_PATH("refJsonPath", "\$.ref")

        private final String key
        private final String value

        HookArgs(String key, String value) {
            this.key = key
            this.value = value
        }

        public String getKey() { return key }

        public String getValue() { return value }
    }
    
    @Override
    protected String branchSpec() {
        return String.format(branchSpec, Configuration.get('pr_number'))
    }
    
    @Override
    public def webHookArgs() {
        return HookArgs.values().collectEntries {
            [(it.getKey()): it.getValue()]
        }
    }
    
    @Override
    public void commentPR(res) {
        def userName = ""
        def userPassword = ""
        context.withCredentials([context.usernamePassword(credentialsId: this.credentialsId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
            userName = context.env.USERNAME
            userPassword = context.env.PASSWORD
        }

        def commit= Configuration.get("pr_sha")

        def consoleLog = Configuration.get(Configuration.Parameter.JOB_URL) + "/" + Configuration.get(Configuration.Parameter.BUILD_NUMBER) + "/console"
        logger.info("consoleLog: ${consoleLog}")
        
        def cmdCurl = "curl https://api.github.com/repos/${userName}/carina-demo/statuses/${commit}?access_token=${userPassword}  -H \"Content-Type: application/json\" -X POST -d {\"state\": \"success\",\"context\": \"compilation checker\", \"description\": \"State\", \"target_url\": \"${consoleLog}\"}"
                
        if (res.equals(BuildResult.FAILURE)) {
            // send to scm that PR checker failed
            cmdCurl = "curl https://api.github.com/repos/${userName}/carina-demo/statuses/${commit}?access_token=${userPassword}  -H \"Content-Type: application/json\" -X POST -d {\"state\": \"failure\",\"context\": \"compilation checker\", \"description\": \"State\", \"target_url\": \"${consoleLog}\"}"
        }
        logger.info("cmdCurl: ${cmdCurl}")
        
        if (context.isUnix()) {
            context.sh cmdCurl
        } else {
            context.bat cmdCurl
        }
    }

}