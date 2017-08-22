package hello;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.Acl.Role;
import com.google.cloud.storage.Acl.User;

public class StorageUtil {
	public final static String BUCKET_NAME = "test-24203634";
	
	public static void write(String fileName, String content) throws IOException {
		InputStream keyStream = StorageUtil.class.getClassLoader().getResourceAsStream("key.json");
		
		// Define the Google cloud storage
		Storage storage = StorageOptions.newBuilder()
			    .setCredentials(ServiceAccountCredentials.fromStream(keyStream))
			    .build()
			    .getService();

		// Upload a file to bucket
		BlobInfo blobInfo =
			      storage.create(
			          BlobInfo
			              .newBuilder(StorageUtil.BUCKET_NAME, fileName)
			              // Modify access list to allow all users with link to read file
			              .setAcl(new ArrayList<>(Arrays.asList(Acl.of(User.ofAllUsers(), Role.READER))))
			              .build(),
			              new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
	
	}
}
