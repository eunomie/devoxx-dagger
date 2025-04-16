package io.dagger.modules.devoxxdagger;

import static io.dagger.client.Dagger.dag;

import io.dagger.client.*;
import io.dagger.module.annotation.DefaultPath;
import io.dagger.module.annotation.Function;
import io.dagger.module.annotation.Object;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** DevoxxDagger main object */
@Object
public class DevoxxDagger {
  @Function
  public String signoff(@DefaultPath("/") Directory source, Secret token)
      throws ExecutionException, DaggerQueryException, InterruptedException {
    Signoff s = dag().signoff(source, token);

    // ensure the repository is clean
    s.isClean();

    // ensure tests are passing
    String testOutput = test(source);
    System.out.println(testOutput);

    // signoff the commit
    s.create();

    // open the PR if needed
    boolean pr = s.hasOpenedPr();
    if (!pr) {
      return s.openPr(new Signoff.OpenPrArguments().withVerbose(true));
    }

    return "ok";
  }

  /** Return the result of running unit tests */
  @Function
  public String test(@DefaultPath("/") Directory source)
      throws InterruptedException, ExecutionException, DaggerQueryException {
    return this.buildEnv(source).withExec(List.of("npm", "run", "test:unit", "run")).stdout();
  }

  /** Build a ready-to-use development environment */
  @Function
  public Container buildEnv(@DefaultPath("/") Directory source) {
    CacheVolume nodeCache = dag().cacheVolume("node");
    return dag()
        .container()
        .from("node:21-slim")
        .withDirectory("/src", source)
        .withMountedCache("/root/.npm", nodeCache)
        .withWorkdir("/src")
        .withExec(List.of("npm", "install"));
  }
}
