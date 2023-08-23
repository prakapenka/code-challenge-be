package localhost.challenge.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.util.ResourceUtils;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

public final class LoadFileUtil {

  private LoadFileUtil() {}

  public static String loadJsonFileAsString(String path) throws IOException {
    final var file = ResourceUtils.getFile(path);
    return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
  }
}
