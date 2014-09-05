# Contributor Guidelines

## Sign the contributor license agreement

Before we can accept your Spring for Android contribution, it is very important that you sign the Contributor License Agreement (CLA). Signing the contributor's agreement does not grant anyone commit rights to the main repository, but it does mean that we can accept your contributions and you will receive author credit. Active contributors might be asked to join the core team and provided the ability to merge pull requests. In order to read and sign the CLA, please visit:

* https://support.springsource.com/spring_committer_signup

As **Project**, please select **Spring for Android**. The **Project Lead** is **Roy Clarkson**.


## Fork the Repository

1. go to [https://github.com/spring-projects/spring-android](https://github.com/spring-projects/spring-android)
2. hit the "fork" button and choose your own github account as the target
3. for more detail see [http://help.github.com/fork-a-repo/](http://help.github.com/fork-a-repo/)

## Setup your Local Development Environment

1. `git clone git@github.com:<your-github-username>/spring-android.git`
2. `cd spring-android`
3. `git remote show`  
_you should see only 'origin' - which is the fork you created for your own github account_
4. `git remote add upstream git@github.com:spring-projects/spring-android.git`
5. `git remote show`  
_you should now see 'upstream' in addition to 'origin' where 'upstream' is the spring-projects repository from which releases are built_
6. `git fetch --all`
7. `git branch -a`
_you should see branches on origin as well as upstream, including 'master'_


## A Day in the Life of a Contributor

* _Always_ work on topic branches.
* For example, to create and switch to a new branch for issue ANDROID-123: `git checkout -b ANDROID-123`
* You might be working on several different topic branches at any given time, but when at a stopping point for one of those branches, commit (a local operation).
* Then to begin working on another issue (say ANDROID-101): `git checkout ANDROID-101`. The _-b_ flag is not needed if that branch already exists in your local repository.
* When ready to resolve an issue or to collaborate with others, you can push your branch to origin (your fork), e.g.: `git push origin ANDROID-123`
* If you want to collaborate with another contributor, have them fork your repository (add it as a remote) and `git fetch <your-username>` to grab your branch. Alternatively, they can use `git fetch --all` to sync their local state with all of their remotes.
* If you grant that collaborator push access to your repository, they can even apply their changes to your branch.
* When ready for your contribution to be reviewed for potential inclusion in the master branch of the canonical spring-android repository (what you know as 'upstream'), issue a pull request to the Spring repository (for more detail, see [http://help.github.com/send-pull-requests/](http://help.github.com/send-pull-requests/)).
* The project lead may merge your changes into the upstream master branch as-is, he may keep the pull request open yet add a comment about something that should be modified, or he might reject the pull request by closing it.
* A prerequisite for any pull request is that it will be cleanly merge-able with the upstream master's current state. **This is the responsibility of any contributor.** If your pull request cannot be applied cleanly, the project lead will most likely add a comment requesting that you make it merge-able.


## Keeping your Local Code in Sync

* As mentioned above, you should always work on topic branches (since 'master' is a moving target). However, you do want to always keep your own 'origin' master branch in synch with the 'upstream' master.
* Within your local working directory, you can sync up all remotes' branches with: `git fetch --all`
* While on your own local master branch: `git pull upstream master` (which is the equivalent of fetching upstream/master and merging that into the branch you are in currently)
* Now that you're in synch, switch to the topic branch where you plan to work, e.g.: `git checkout -b ANDROID-123`
* When you get to a stopping point: `git commit`
* If changes have occurred on the upstream/master while you were working you can synch again:
    - Switch back to master: `git checkout master`
    - Then: `git pull upstream master`
    - Switch back to the topic branch: `git checkout ANDROID-123` (no -b needed since the branch already exists)
    - Rebase the topic branch to minimize the distance between it and your recently synched master branch: `git rebase master`
* **Note** that you can always force push (git push -f) reworked / rebased commits against the branch used to submit your pull request. In other words, you do not need to issue a new pull request when asked to make changes.
* Now, if you issue a pull request, it is much more likely to be merged without conflicts. Most likely, any pull request that would produce conflicts will be deferred until the issuer of that pull request makes these adjustments.
* Assuming your pull request is merged into the 'upstream' master, you will actually end up pulling that change into your own master eventually, and at that time, you may decide to delete the topic branch from your local repository and your fork (origin) if you pushed it there.
    - to delete the local branch: `git branch -d ANDROID-123`
    - to delete the branch from your origin: `git push origin :ANDROID-123`


## Additional Information

See The Spring Framework [contributor guidelines](https://github.com/spring-projects/spring-framework/blob/master/CONTRIBUTING.md) for more details about contributing to Spring projects.