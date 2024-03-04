package com.pim.Merchandise.PINServiceUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.EncryptedPutObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.gson.Gson;
import com.mongodb.bulk.BulkWriteResult;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.pim.Merchandise.model.PIMFileList;
import com.pim.Merchandise.model.PIMProductTracker;
import com.pim.Merchandise.model.Product;
import com.pim.Merchandise.model.ProductInfo;
import com.pim.Merchandise.repository.MongodbRepository;

@Service
public class PIMService {
	Logger logger = LoggerFactory.getLogger(PIMService.class);
	
	@Autowired
	private Gson gson;
	@Autowired
	private MongodbRepository mongodbRepository;
	@Autowired
	private MongoTemplate mongoTemplate;
	
	public String processCatalogData(String contentId) {
		AWSCredentials credentials = new BasicAWSCredentials("xxxx","xxx");
		AmazonS3 s3client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.AP_SOUTH_1).build();
		GetObjectRequest getObjectRequest = new GetObjectRequest("kloud4-export", contentId);
		S3Object s3objectResponse = s3client.getObject(getObjectRequest);
		return readandupdateCatalog(s3objectResponse,contentId);
		
	}
	
	public String removeCatalogData(String contentId) {
		AWSCredentials credentials = new BasicAWSCredentials("xxxx","xxx");
		AmazonS3 s3client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.AP_SOUTH_1).build();
		DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest("kloud4-export", contentId);
		s3client.deleteObject(deleteObjectRequest);
		return "File deleted in S3";
		
	}
	
	public List<PIMFileList> listCatalogDataFiles() {
		AWSCredentials credentials = new BasicAWSCredentials("xx","xxxy");
		AmazonS3 s3client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.AP_SOUTH_1).build();
		ObjectListing objectList = s3client.listObjects("kloud4-export");
		List<S3ObjectSummary> seObjectSummryList = objectList.getObjectSummaries();
		PIMFileList pimFileList = null;
		 List<PIMFileList>  pimFileLists = new ArrayList<PIMFileList>();
		for(S3ObjectSummary s3ObjectSummary : seObjectSummryList) {
			pimFileList = new PIMFileList();
			pimFileList.setFileName(s3ObjectSummary.getKey());
			pimFileList.setLastModifiedDate(s3ObjectSummary.getLastModified());
			populatePIMFileStatus(pimFileList, s3ObjectSummary.getKey());
			pimFileLists.add(pimFileList);
		}
		
		return pimFileLists;
		
	}
	
	private void populatePIMFileStatus(PIMFileList pimFileList,String fileName) {
		List<PIMProductTracker> pimProductTrackerList = mongodbRepository.findAll(fileName);
		if(pimProductTrackerList != null && pimProductTrackerList.size() == 0)
			pimFileList.setStatus("New");
		else
			pimFileList.setStatus(pimProductTrackerList.get(0).getStatus());
		
	}
	
	public String addCatalogDataFile(MultipartFile file) throws IOException  {
		AWSCredentials credentials = new BasicAWSCredentials("xxx","xxx");
		AmazonS3 s3client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.AP_SOUTH_1).build();
		PutObjectRequest putObjectRequest = new EncryptedPutObjectRequest("kloud4-export", file.getOriginalFilename(), file.getInputStream(), null);
		s3client.putObject(putObjectRequest);
		return "File uploaded success";
		
	}
	
	public String reprocessCatalogData(String contentId) {
		AWSCredentials credentials = new BasicAWSCredentials("xxxx","xxx");
		AmazonS3 s3client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.AP_SOUTH_1).build();
		GetObjectRequest getObjectRequest = new GetObjectRequest("kloud4-export", contentId);
		S3Object s3objectResponse = s3client.getObject(getObjectRequest);
		return readandupdateCatalog(s3objectResponse,contentId);
		
	}
	
	private String readandupdateCatalog(S3Object s3objectResponse,String contentId) {
		try { 
			BufferedReader reader = new BufferedReader(new InputStreamReader(s3objectResponse.getObjectContent()));	
	        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
	       CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).withSkipLines(1).build();
	      // CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build();
	        List<String[]> allData = csvReader.readAll(); 
	        int count = 0;
	        ProductInfo productData = null;
	        List<Product> productInfoList = new ArrayList<Product>();
            for (String[] productRow : allData) { 
            	String[] productStringArry = new String[productRow.length];
            	productData = new ProductInfo();
            	count = 0;
                for (String productInfo : productRow) { 
                	if(count == 19 || count == 24 || count == 25 || count == 42 || count == 43 || count == 55 || count == 56 || count == 56 || count == 45 || count == 48 || count == 62 || count == 65) {
                		byte ptext[] = productInfo.getBytes("ISO-8859-1"); 
                		String actualValue = new String(ptext, "UTF-8"); 
                		productStringArry[count] = actualValue;
                	} else {
                		productStringArry[count] = productInfo;
                	}
                	count++;
                } 
                productData = populateCatalogData(productData, productStringArry);
                productInfoList.add(convertToDbObject(productData));
            } 
            commitToDB(productInfoList);
            updateFileStatus(contentId);;
            return "Product catalog data populated";
		} catch (Exception e) { 
			e.printStackTrace();
	        logger.info("----Error ocurred while reading Catalog csv from Akeneo PIM");
	     } 
		
		return "";
	}
	
	private void updateFileStatus(String fileName) {
		List<PIMProductTracker> pimProductTrackerList = mongodbRepository.findAll(fileName);
		if(pimProductTrackerList == null ||pimProductTrackerList.size() == 0) {
			PIMProductTracker pimProductTracker = new PIMProductTracker();
			pimProductTracker.setFileName(fileName);
			pimProductTracker.setStatus("Processed");
			mongoTemplate.insert(pimProductTracker);
		} 
		
	}
	private Product convertToDbObject(ProductInfo productInfo) {
		String jsonConvertedString = gson.toJson(productInfo);
		return gson.fromJson(jsonConvertedString, Product.class);
	}
	
	private void commitToDB(List<Product> productInfoList) {
		try {
			BulkOperations bulkOps = mongoTemplate.bulkOps(BulkMode.UNORDERED, Product.class);
			productInfoList.stream().filter(product -> product != null).forEach(product -> {
				org.bson.Document dbDoc = new org.bson.Document().append("$set", product);
	            mongoTemplate.getConverter().write(product, dbDoc);
	            Query query = new Query().addCriteria(new Criteria("uuId").is(product.getUuId()));
	            Update update = Update.fromDocument(dbDoc, "");
	            bulkOps.upsert(query, update);
	        });

	        BulkWriteResult result = bulkOps.execute();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private ProductInfo populateCatalogData(ProductInfo productData,String[] productArry) {
		productData.setUuId(productArry[0]);
		productData.setProductId(productArry[1]);
    	productData.setCategories(productArry[2]);
    	productData.setEnabled(productArry[3]);
    	productData.setCategory(productArry[4]);
    	if("Clothing".equalsIgnoreCase(productArry[4])) {
    		List<String> sizes = new ArrayList<String>();
    		sizes.add(productArry[16]);
    		productData.setSizes(sizes);
    		List<String> colors = new ArrayList<String>();
    		colors.add(productArry[18]);
    		productData.setColors(colors);
    		productData.setPattern(productArry[19]);
    		productData.setProductDesc(productArry[24]);
    		productData.setStyleName(productArry[63]);
    		productData.setExclusive(productArry[33]);
    		productData.setGenderType(productArry[34]);
    		String imageurl = "https://kloud4-retail.s3.ap-south-1.amazonaws.com/category/"+productArry[36];
    		productData.setImageURL(imageurl);
    		productData.setImage(productArry[37]);
    		productData.setOnlineDate(productArry[35]);
    		productData.setProductName(productArry[51]);
    		productData.setPrice(productArry[57]);
    		productData.setMaterial(productArry[49]);
    		productData.setMadeIn(productArry[48]);
    	} else if("Bags".equalsIgnoreCase(productArry[4])) {
    		List<String> colors = new ArrayList<String>();
    		colors.add(productArry[11]);
    		productData.setColors(colors);
    		productData.setPattern(productArry[12]);
    		productData.setProductDesc(productArry[33]);
    		productData.setStyleName(productArry[56]);
    		productData.setExclusive(productArry[26]);
    		productData.setGenderType(productArry[27]);
    		String imageurl = "https://kloud4-retail.s3.ap-south-1.amazonaws.com/category/"+productArry[29];
    		productData.setImageURL(imageurl);
    		productData.setImage(productArry[30]);
    		productData.setOnlineDate(productArry[28]);
    		productData.setProductName(productArry[46]);
    		productData.setPrice(productArry[57]);
    	}  else if("LED TVs".equalsIgnoreCase(productArry[4])) {
    		List<String> colors = new ArrayList<String>();
    		colors.add(productArry[11]);
    		productData.setColors(colors);
    		productData.setProductDesc(productArry[33]);
    		String imageurl = "https://kloud4-retail.s3.ap-south-1.amazonaws.com/category/"+productArry[56];
    		productData.setImageURL(imageurl);
    		productData.setImage(productArry[57]);
    		productData.setProductName(productArry[40]);
    		productData.setPrice(productArry[93]);
    		List<String> sizes = new ArrayList<String>();
    		sizes.add(productArry[116]);
    		productData.setSizes(sizes);
    		productData.setModelNo(productArry[76]);
    		productData.setFeatures(productArry[48]);
    		productData.setBrand(productArry[25]);
    		productData.setBuiltinSpeakers(productArry[26]);
    		productData.setBluetoothEnabled(productArry[23]);
    		productData.setInch(productArry[140]);
    	}
    	
    	return productData;
    	
	}
}
