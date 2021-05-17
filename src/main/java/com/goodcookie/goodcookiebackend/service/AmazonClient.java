package com.goodcookie.goodcookiebackend.service;

import com.goodcookie.goodcookiebackend.exception.GoodCookieBackendException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iot.model.CannedAccessControlList;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class to interact with the
 * Amazon S3 Storage Service
 */
@Service
public class AmazonClient {
    private S3Client s3Client;

    @Value("${amazonProperties.s3.endpointUrl}")
    private String endpointUrl;
    @Value("${amazonProperties.s3.bucketName}")
    private String bucketName;
    @Value("${amazonProperties.s3.accessKey}")
    private String accessKey;
    @Value("${amazonProperties.s3.secretKey}")
    private String secretKey;
    @Value("${amazonProperties.s3.region}")
    private String region;

    @PostConstruct
    private void initializeAmazon() {
        AwsCredentials credentials = AwsBasicCredentials.create(this.accessKey, this.secretKey);
        this.s3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(region))
                .build();
    }


    /**
     * Converts the multipart file received from the controller
     * to a file object that can be uploaded to S3
     * @param file The file to convert
     * @return Converted File
     * @throws IOException in case of failure
     */
    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    /**
     * Converts the base64 image url received from controller
     * to an image file object that can be uploaded to S3
     * @param data64Url the base64 url to convert
     * @return File object converted from url
     * @throws IOException in case of failure
     */
    private File convertBase64ToFile(String data64Url) throws IOException{
       File convFile = new File(UUID.randomUUID().toString()+".png");
       FileOutputStream fos = new FileOutputStream(convFile);

       String base64 = data64Url.split(",")[1];

       byte[] bytes = Base64.getDecoder().decode(base64.getBytes(StandardCharsets.UTF_8));
       fos.write(bytes);
       fos.close();
       return convFile;
    }

    /**
     * Generates a unique fileName for the file that is
     * to be uploaded.
     */
    private String generateFileName(String username){
        return new Date().getTime() + "-" + username + "-journal";
    }

    /**
     * Uploads the file provided to the S3 bucket of the application
     * @param fileName
     * @param file
     */
    private void uploadFileTos3bucket(String fileName, File file) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(this.bucketName)
                .key(fileName)
                //Allow public read from file url
                .acl(CannedAccessControlList.PUBLIC_READ.toString())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
    }

    /**
     * Attempts to upload the provided file to the Amazon S3 bucket
     * @param journalDataUrl the
     * @param username username of user related with journal to save
     * @return A unique file url identifying the file uploaded
     */
    public String uploadFile(String journalDataUrl, String username){
        //Example url https://<bucket-name>.<endpointUrl>/<filename>
        String fileUrl = "";
        try{
            File file = convertBase64ToFile(journalDataUrl);
            String fileName = generateFileName(username);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.endpointUrl);
            //Attach the bucketName after https://
            stringBuilder.insert(8,this.bucketName + ".");
            stringBuilder.append("/").append(fileName);
            fileUrl = stringBuilder.toString();
//            fileUrl = this.endpointUrl + "/" + this.bucketName + "/" + fileName;
            uploadFileTos3bucket(fileName, file);
            file.delete();
        }catch(Exception e){
            throw new GoodCookieBackendException("Error occurred while uploading File to S3");
        }
        return fileUrl;
    }

//    /*
//    TODO: Elimnate this method since deleteManyFiles can replace it
//     */
//    /**
//     * Deletes the file associated with the fileUrl provided
//     * @param fileUrl URL of the file to be deleted
//     */
//    public void deleteFileFromS3Bucket(String fileUrl) {
//        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
//        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
//                .bucket(this.bucketName)
//                .key(fileName)
//                .build();
//        s3Client.deleteObject(deleteObjectRequest);
//        //TODO: catch a response to return to the user
//    }

    /**
     * Deletes a list of files from S3 associated with the given urls
     * @param journalUrlList List of journal urls to delete
     */
    public void deleteManyFilesFromS3Bucket(List<String> journalUrlList) {
        //Extract the filenames of the objects to delete from each url in the list
        List<String> filenames = journalUrlList.stream()
                .map(url -> url.substring(url.lastIndexOf("/") + 1))
                .collect(Collectors.toList());
        //Map each file from the list into an Amazon's ObjectIdentifier object
        List<ObjectIdentifier> toDelete = filenames.stream()
                .map(filename -> ObjectIdentifier.builder().key(filename).build())
                .collect(Collectors.toList());
        try {
            //DOR containing the list of objects to delete
            DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                    .bucket(this.bucketName)
                    .delete(Delete.builder().objects(toDelete).build())
                    .build();
            //Attempt deletion of multiple objects
            DeleteObjectsResponse deleteObjectResponse = s3Client.deleteObjects(deleteObjectsRequest);
            System.out.println("Deleted " + deleteObjectResponse.deleted().size() + " files");
        }
        catch (S3Exception e){
            throw new GoodCookieBackendException("Error Occurred While Attempting to Delete Objects From S3", e);
        }

    }

}
