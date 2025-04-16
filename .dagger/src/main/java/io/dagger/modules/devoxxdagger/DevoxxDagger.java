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
  public String check(@DefaultPath("/") Directory source, Secret token)
      throws ExecutionException, DaggerQueryException, InterruptedException {
    Signoff s = dag().signoff(source, token);

    // ensure the repository is clean
    s.isClean();

    // ensure tests are passing
    test(source);

    // signoff the commit
    s.create();

    String out = "✓ commit %s signed off\n".formatted(s.sha());

    // open the PR if needed
    String pr = s.pullRequest();
    if (pr.isEmpty()) {
      out += "✓ pull request opened: " + s.openPr(new Signoff.OpenPrArguments().withVerbose(true));
    } else {
      out += "✓ existing pull request: " + pr;
    }
    return out;
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
        .withMountedCache("/root/.npm", nodeCache)
        .withDirectory("/src", source)
        .withWorkdir("/src")
        .withExec(List.of("npm", "install"));
  }

  /** Install check on default branch */
  @Function
  public String install(@DefaultPath("/") Directory source, Secret token)
      throws ExecutionException, DaggerQueryException, InterruptedException {
    dag().signoff(source, token).install();
    return "✓️ check installed on default branch";
  }
}
