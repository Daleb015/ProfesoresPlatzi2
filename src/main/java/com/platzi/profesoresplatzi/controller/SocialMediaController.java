package com.platzi.profesoresplatzi.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.platzi.profesoresplatzi.model.SocialMedia;
import com.platzi.profesoresplatzi.model.Teacher;
import com.platzi.profesoresplatzi.service.SocialMediaService;
import com.platzi.profesoresplatzi.util.CustomErrorType;

@Controller
@RequestMapping("/v1")
public class SocialMediaController {

  @Autowired
  SocialMediaService socialmediaservice;

  @RequestMapping(value = "/socialMedias", method = RequestMethod.GET, headers = "Accept=application/json")
  private ResponseEntity<List<SocialMedia>> getSocialMedias(
      @RequestParam(value = "name", required = false) String name) {

    List<SocialMedia> socialMedias = new ArrayList<>();

    if (name == null) {
      socialMedias = socialmediaservice.findAllSocialMedias();

      if (socialMedias.isEmpty()) {
        return new ResponseEntity(HttpStatus.NO_CONTENT);
      }

      return new ResponseEntity<List<SocialMedia>>(socialMedias, HttpStatus.OK);
    } else {
      SocialMedia socialMedia = socialmediaservice.findByName(name);
      if (socialMedia == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

      socialMedias.add(socialMedia);
      return new ResponseEntity<List<SocialMedia>>(socialMedias, HttpStatus.OK);
    }

  }

  @RequestMapping(value = "/socialMedias", method = RequestMethod.POST, headers = "Accept=application/json")
  public ResponseEntity<?> createSocialMedia(@RequestBody SocialMedia socialMedia,
      UriComponentsBuilder uriComponentsBuilder) {
    if (socialMedia.getName().equals(null) || socialMedia.getName().isEmpty()) {
      return new ResponseEntity(new CustomErrorType("El campo nombre no es valido"),
          HttpStatus.CONFLICT);
    }

    if (socialmediaservice.findByName(socialMedia.getName()) != null) {
      return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    socialmediaservice.saveSocialMedia(socialMedia);
    SocialMedia socialMedia2 = socialmediaservice.findByName(socialMedia.getName());
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(uriComponentsBuilder.path("/v1/socialMedias/{id}")
        .buildAndExpand(socialMedia2.getIdSocialMedia()).toUri());

    return new ResponseEntity<String>(headers, HttpStatus.CREATED);

  }

  @RequestMapping(value = "/socialMedias/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
  public ResponseEntity<SocialMedia> getSocialMediaById(@PathVariable("id") Long id) {

    if (id == null || id <= 0) {
      return new ResponseEntity(new CustomErrorType("El campo id no es valido"),
          HttpStatus.CONFLICT);
    }

    SocialMedia socialMedia = socialmediaservice.findById(id);

    if (socialMedia == null) {
      return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    return new ResponseEntity<SocialMedia>(socialMedia, HttpStatus.OK);

  }

  @RequestMapping(value = "/socialMedias/{id}", method = RequestMethod.PATCH, headers = "Accept=application/json")
  public ResponseEntity<?> updateSocialMedia(@PathVariable("id") Long idsocialMedia,
      @RequestBody SocialMedia socialMedia) {

    SocialMedia currentSocialMedia = socialmediaservice.findById(idsocialMedia);

    if (currentSocialMedia == null) {
      return new ResponseEntity(new CustomErrorType("El campo id no es valido"),
          HttpStatus.CONFLICT);
    }

    currentSocialMedia.setName(socialMedia.getName());
    currentSocialMedia.setIcon(socialMedia.getIcon());

    socialmediaservice.updateSocialMedia(currentSocialMedia);

    return new ResponseEntity<SocialMedia>(currentSocialMedia, HttpStatus.OK);

  }

  @RequestMapping(value = "/socialMedias/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteSocialMedia(@PathVariable("id") Long idSocialMedia) {
    if (idSocialMedia == null || idSocialMedia <= 0) {
      return new ResponseEntity(new CustomErrorType("El campo id no es valido"),
          HttpStatus.CONFLICT);
    }

    SocialMedia socialMedia = socialmediaservice.findById(idSocialMedia);

    if (socialMedia == null) {
      return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    socialmediaservice.deleteSocialMediaById(idSocialMedia);

    return new ResponseEntity<SocialMedia>(HttpStatus.OK);

  }

  public static final String SOCIALMEDIA_UPLOADED_FOLDER = "images/socialMedias/";

  @RequestMapping(value = "/socialmedias/image", method = RequestMethod.POST, headers = ("content-type=multipart/form-data"))
  public ResponseEntity<byte[]> uploadSocialMediaImage(@RequestParam("id") Long id,
      @RequestParam("file") MultipartFile multipartFile,
      UriComponentsBuilder uriComponentsBuilder) {

    if (id == null) {
      return new ResponseEntity(new CustomErrorType("Id socialmedia  invalido"),
          HttpStatus.NO_CONTENT);
    }

    if (multipartFile == null) {
      return new ResponseEntity(new CustomErrorType("File  invalido"), HttpStatus.NO_CONTENT);
    }

    SocialMedia socialMedia = socialmediaservice.findById(id);

    if (socialMedia == null) {
      return new ResponseEntity(new CustomErrorType("No exiete el socialmedia elegido"),
          HttpStatus.NO_CONTENT);
    }

    if (!socialMedia.getIcon().isEmpty() || socialMedia.getIcon() != null) {
      String fileName = socialMedia.getIcon();
      Path path = Paths.get(fileName);
      File f = path.toFile();
      if (f.exists()) {
        f.delete();
      }
    }

    try {
      Date date = new Date();
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
      String dateName = dateFormat.format(date);
      String fileName = String.valueOf(id) + "-iconsocialmedia" + dateName + "."
          + multipartFile.getContentType().split("/")[1];
      socialMedia.setIcon(SOCIALMEDIA_UPLOADED_FOLDER + fileName);
      byte[] bytes = multipartFile.getBytes();
      Path path = Paths.get(SOCIALMEDIA_UPLOADED_FOLDER + fileName);
      Files.write(path, bytes);
      socialmediaservice.updateSocialMedia(socialMedia);
      return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity(
          new CustomErrorType("Error al subir: " + multipartFile.getOriginalFilename()),
          HttpStatus.NO_CONTENT);
    }

  }
  
  @RequestMapping(value = "/socialmedias/{id_socialmedia}/images/", method = RequestMethod.GET)
  public ResponseEntity<byte[]> getSocialMediaImage(@PathVariable("id_socialmedia") Long idsocialmedia) {
    if (idsocialmedia == null) {
      return new ResponseEntity(new CustomErrorType("Ingrese id socialmedia valido"),
          HttpStatus.NO_CONTENT);
    }

    SocialMedia socialMedia = socialmediaservice.findById(idsocialmedia);

    if (socialMedia == null) {
      return new ResponseEntity(new CustomErrorType("No existe el socialmedia con el id enviado"),
          HttpStatus.NOT_FOUND);
    }

    try {
      String fileName = socialMedia.getIcon();
      Path path = Paths.get(fileName);
      File f = path.toFile();
      System.out.println(f);
      if (!f.exists()) {
        return new ResponseEntity(new CustomErrorType("Imagen no encontrada"), HttpStatus.CONFLICT);
      }

      byte[] image = Files.readAllBytes(path);
      return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);

    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity(new CustomErrorType("error al mostrar imagen"),
          HttpStatus.CONFLICT);
    }

  }
  
  @RequestMapping(value = "/socialmedias/{id_socialmedia}/images", method = RequestMethod.DELETE, headers = "Accept=application/json")
  public ResponseEntity<?> deletesocialMediaImage(@PathVariable("id_socialmedia") Long idsocialmedia) {
    if (idsocialmedia == null) {
      return new ResponseEntity(new CustomErrorType("Ingrese id socialmedia valido"),
          HttpStatus.NO_CONTENT);
    }

    SocialMedia socialMedia = socialmediaservice.findById(idsocialmedia);

    if (socialMedia == null) {
      return new ResponseEntity(new CustomErrorType("No existe el socialmedia con el id enviado"),
          HttpStatus.NOT_FOUND);
    }
    
    if (socialMedia.getIcon().isEmpty()||socialMedia.getIcon()==null) {
      return new ResponseEntity(new CustomErrorType("No tiene imagen asignada"),
          HttpStatus.NOT_FOUND);
    }
    
    String fileName = socialMedia.getIcon();
    Path path = Paths.get(fileName);
    File file = path.toFile();
    if (file.exists()) {
      file.delete();
    }
    
    socialMedia.setIcon("");
    socialmediaservice.updateSocialMedia(socialMedia);
    return new ResponseEntity<SocialMedia>(HttpStatus.NO_CONTENT);

  }
}
