package dev.memocode.awss3presignedurl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@RestController
public class PresignedURLController {

    private final static String BUCKET_NAME = "test-memocode-1";

    @PostMapping("/presignedPutURL")
    public ResponseEntity<String> createPresignedPutURL() {
        try (
                // S3Presigner는 presignedURL을 생성하는 객체입니다.
                S3Presigner presigner = S3Presigner.create()
        ) {
            // PutObjectRequest는 S3에 저장하고자 하는 요청 객체를 정의합니다.
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME) // 버킷 이름
                    .key(UUID.randomUUID() + ".jpeg") // 객체 이름
                    .contentType("image/jpeg") // contentType을 지정하여 다른 확장자를 올릴 수 없도록 제한을 걸 수 있습니다.
                    .build();

            // PutObjectPresignRequest는 presignedURL put 요청을 하기 위한 객체를 정의합니다.
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(5)) // 해당 URL의 만료시간
                    .putObjectRequest(objectRequest) // S3에 저장하고자 하는 요청 객체
                    .build();

            // presignedURL 요청
            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

            // presignedURL
            String presignedURL = presignedRequest.url().toExternalForm();
            log.info("PresignedURL: {}", presignedURL);
            log.info("PresignedURL HTTP Method: {}", presignedRequest.httpRequest().method());

            return ResponseEntity
                    .created(URI.create(presignedURL))
                    .body(presignedURL);
        }
    }
}
