# Contributing

## Commit messages
Before writing a commit message read [this article](https://chris.beams.io/posts/git-commit/).

## Build
Before pushing any changes make sure project builds without errors with:

```
./gradlew build
```

## Unit tests
This project uses [Kotest](https://kotest.io/) for testing.

- please use the `Spec` suffix for new test classes
- make sure tests clearly document new feature

## Validate changes locally
Before submitting a pull request test your changes locally on a sample project.
There are few ways for local testing:

- simply use one of the [sample subprojects](https://github.com/coditory/klog/tree/master/samples)
- or publish library to maven local repository with `./gradlew publishToMavenLocal` and use it in any project
  via [`mavenLocal()`](https://docs.gradle.org/current/userguide/declaring_repositories.html#sub:maven_local) repository

## Validating with snapshot release
Snapshot release is triggered automatically after merge to the main branch.
To use a released snapshot version make sure to register Sonatype snapshot repository in gradle with:

```
// build.gradle.kts
repositories {
    mavenCentral()
    maven {
        url = URI("https://oss.sonatype.org/content/repositories/snapshots")
    }
}
```

The snapshot version can be found in GitHub Action build log.

## Documentation
If change adds new feature or modifies a new one
update [documentation](https://github.com/coditory/klog/tree/master/samples).
