package com.zebrunner.jenkins.jobdsl.factory.pipeline.scm

import com.zebrunner.jenkins.jobdsl.factory.pipeline.PipelineFactory

import groovy.transform.InheritConstructors
import com.cloudbees.groovy.cps.NonCPS

@InheritConstructors
public class MergeJobFactory extends PipelineFactory {

    def host
    def organization
    def repo
    def scmRepoUrl

    public MergeJobFactory(folder, pipelineScript, jobName, host, organization, repo, scmRepoUrl) {
        this.folder = folder
        this.pipelineScript = pipelineScript
        this.name = jobName
        this.description = getDesc()
        this.host = host
        this.organization = organization
        this.repo = repo
        this.scmRepoUrl = scmRepoUrl
    }

    def create() {
        def pipelineJob = super.create()

        pipelineJob.with {
            properties {
                //TODO: add SCM artifacts
                githubProjectUrl(scmRepoUrl)
            }

            //TODO: think about other parameters to support DevOps CI operations
            parameters {
                configure addHiddenParameter('GITHUB_HOST', '', host)
                configure addHiddenParameter('GITHUB_ORGANIZATION', '', organization)
                configure addHiddenParameter('repo', 'GitHub repository for merging', repo)
                stringParam('branch', 'master', 'Source SCM repository branch')
                stringParam('targetBranch', 'STAG', 'Target SCM repository branch')
                booleanParam('forcePush', false, 'If chosen, do force branches merge.')
            }

        }
        return pipelineJob
    }

    @NonCPS
    private def getDesc() {
        return "SCM branch merger job"
    }
}