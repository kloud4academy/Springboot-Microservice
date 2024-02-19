package com.pim.Merchandise;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.pim.Merchandise.PINServiceUtils.PIMService;
import com.pim.Merchandise.model.PIMFileList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@CrossOrigin
@Controller
public class PIMRestController {
	Logger logger = LoggerFactory.getLogger(PIMRestController.class);
    @Autowired
    private Gson gson;
   // @Autowired
    //private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PIMService pimService;

    @RequestMapping(path="/pim/processCatalogData/{contentId}",method = RequestMethod.GET)
	public ModelAndView processCatalogData(@PathVariable String contentId) {
    	ModelAndView mv = new ModelAndView();
		try {
			pimService.processCatalogData(contentId);
			List<PIMFileList> catalogFiles = pimService.listCatalogDataFiles();
			mv.addObject("catalogFiles", catalogFiles);
		} catch(Exception e) {
			logger.error("Error while reading catalog csv file from S3 Bucket."+e.getMessage());
		}
		mv.setViewName("catalogsynch");
		return mv;
	}
    
    @PostMapping(path="/pim/uploadcatalog")
	public ModelAndView addCatalogData(@RequestParam("file") MultipartFile file,HttpSession session, HttpServletResponse response,HttpServletRequest request) {
		ModelAndView mv = new ModelAndView();
		try {
			if(file == null) {
				return new ModelAndView("redirect:/pim/viewcatalog");
			}
			pimService.addCatalogDataFile(file);
			List<PIMFileList> catalogFiles = pimService.listCatalogDataFiles();
			mv.addObject("succesMsg", "File uploadaed into content server,You can process to Ecommerce catalog at any time!");
			mv.addObject("catalogFiles", catalogFiles);
		} catch(Exception e) {
			logger.error("error while uploading catalog into S3...."+e.getMessage());
		}
		mv.setViewName("catalogsynch");
		return mv;
	}
    
    @RequestMapping(path="/pim/viewcatalog",method = RequestMethod.GET)
	public ModelAndView addCatalogData(HttpSession session, HttpServletResponse response,HttpServletRequest request) {
		ModelAndView mv = new ModelAndView();
		try {
			List<PIMFileList> catalogFiles = pimService.listCatalogDataFiles();
			mv.addObject("catalogFiles", catalogFiles);
		} catch(Exception e) {
			logger.error("error while reading catalog from S3....."+e.getMessage());
		}
		mv.addObject("succesMsg", "");
		mv.setViewName("catalogsynch");
		return mv;
	}
    
    @RequestMapping(path="/pim/deletecatalog/{contentId}",method = RequestMethod.GET)
	public ModelAndView deleteCatalogData(@PathVariable String contentId,HttpSession session, HttpServletResponse response,HttpServletRequest request) {
    	if(contentId == null) {
			return new ModelAndView("redirect:/pim/viewcatalog");
		}
    	String responseString = null;
		ModelAndView mv = new ModelAndView();
		try {
			responseString = pimService.removeCatalogData(contentId);
			List<PIMFileList> catalogFiles = pimService.listCatalogDataFiles();
			mv.addObject("succesMsg", "");
			mv.addObject("catalogFiles", catalogFiles);
		} catch(Exception e) {
			logger.error("create cart error in delete cart...."+e.getMessage());
		}
		mv.setViewName("catalogsynch");
		return mv;
	}
}
