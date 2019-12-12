package com.platzi.profesoresplatzi.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.platzi.profesoresplatzi.model.Teacher;
import com.platzi.profesoresplatzi.service.TeacherService;
import com.platzi.profesoresplatzi.util.CustomErrorType;

@Controller
@RequestMapping("/v1")
public class TeacherController {
  @Autowired
  TeacherService teacherService;

  @RequestMapping(value = "/teachers", method = RequestMethod.GET, headers = "Accept=application/json")
  private ResponseEntity<List<Teacher>> getTeacher(
      @RequestParam(value = "name", required = false) String name) {
    List<Teacher> teachers = new ArrayList<>();

    if (name == null) {
      teachers = teacherService.findAllTeachers();

      if (teachers.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }

      return new ResponseEntity<List<Teacher>>(teachers, HttpStatus.OK);

    } else {
      Teacher teacher = teacherService.findByName(name);
      if (teacher == null) {
        return new ResponseEntity<List<Teacher>>(teachers, HttpStatus.OK);
      }

      teachers.add(teacher);
      return new ResponseEntity<List<Teacher>>(teachers, HttpStatus.OK);
    }

  }

  @RequestMapping(value = "/teachers", method = RequestMethod.POST, headers = "Accept=application/json")
  public ResponseEntity<?> createTeacher(@RequestBody Teacher teacher,
      UriComponentsBuilder uriComponentsBuilder) {
    if (teacher.getName().equals(null) || teacher.getName().isEmpty()) {
      return new ResponseEntity(new CustomErrorType("El campo nombre no es valido"),
          HttpStatus.CONFLICT);
    }

    if (teacherService.findByName(teacher.getName()) != null) {
      return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    teacherService.saveTeacher(teacher);
    Teacher teacher2 = teacherService.findByName(teacher.getName());
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(uriComponentsBuilder.path("/v1/teachers/{id}")
        .buildAndExpand(teacher2.getIdTeacher()).toUri());
    return new ResponseEntity<String>(headers, HttpStatus.CREATED);

  }

  @RequestMapping(value = "/teachers/{id}", method = RequestMethod.PATCH, headers = "Accept=application/json")
  public ResponseEntity<?> updateTeacher(@PathVariable("id") Long id,
      @RequestBody Teacher teacher) {
    Teacher currentTeacher = teacherService.findById(id);
    if (currentTeacher == null) {
      return new ResponseEntity(new CustomErrorType("El campo id no es valido"),
          HttpStatus.CONFLICT);
    }

    currentTeacher.setAvatar(teacher.getAvatar());
    currentTeacher.setName(teacher.getName());

    teacherService.updateTeacher(currentTeacher);

    return new ResponseEntity<Teacher>(currentTeacher, HttpStatus.OK);

  }

  @RequestMapping(value = "/teacher/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
  public ResponseEntity<Teacher> getTeacherById(@PathVariable("id") Long id) {
    if (id == null || id <= 0) {
      return new ResponseEntity(new CustomErrorType("El campo id no es valido"),
          HttpStatus.CONFLICT);
    }

    Teacher teacher = teacherService.findById(id);

    if (teacher == null) {
      return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    return new ResponseEntity<Teacher>(teacher, HttpStatus.OK);

  }

  @RequestMapping(value = "/teachers/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteTeacher(@PathVariable("id") Long id) {
    if (id == null || id <= 0) {
      return new ResponseEntity(new CustomErrorType("El campo id no es valido"),
          HttpStatus.CONFLICT);
    }

    Teacher teacher = teacherService.findById(id);

    if (teacher == null) {
      return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
    teacherService.deleteTeacherById(id);

    return new ResponseEntity<Teacher>(HttpStatus.OK);

  }

  public static final String TEACHER_UPLOADED_FOLDER = "images/teachers/";

  @RequestMapping(value = "/teachers/image", method = RequestMethod.POST, headers = ("content-type=multipart/form-data"))
  public ResponseEntity<byte[]> uploadTeacherImage(@RequestParam("id") Long id,
      @RequestParam("file") MultipartFile multipartFile,
      UriComponentsBuilder uriComponentsBuilder) {
    if (id == null) {
      return new ResponseEntity(new CustomErrorType("Id teacher  invalido"), HttpStatus.NO_CONTENT);
    }

    if (multipartFile == null) {
      return new ResponseEntity(new CustomErrorType("File  invalido"), HttpStatus.NO_CONTENT);
    }

    Teacher teacher = teacherService.findById(id);

    if (teacher == null) {
      return new ResponseEntity(new CustomErrorType("No exiete el teacher elegido"),
          HttpStatus.NO_CONTENT);
    }

    if (!teacher.getAvatar().isEmpty() || teacher.getAvatar() != null) {
      String fileName = teacher.getAvatar();
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
      String fileName = String.valueOf(id) + "-pictureteacher" + dateName + "."
          + multipartFile.getContentType().split("/")[1];
      teacher.setAvatar(TEACHER_UPLOADED_FOLDER + fileName);
      byte[] bytes = multipartFile.getBytes();
      Path path = Paths.get(TEACHER_UPLOADED_FOLDER + fileName);
      Files.write(path, bytes);
      teacherService.updateTeacher(teacher);
      return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity(
          new CustomErrorType("Error al subir: " + multipartFile.getOriginalFilename()),
          HttpStatus.NO_CONTENT);
    }

  }

  @RequestMapping(value = "/teachers/{id_teacher}/images/", method = RequestMethod.GET)
  public ResponseEntity<byte[]> getTeacherImage(@PathVariable("id_teacher") Long idTeacher) {
    if (idTeacher == null) {
      return new ResponseEntity(new CustomErrorType("Ingrese id teacher valido"),
          HttpStatus.NO_CONTENT);
    }

    Teacher teacher = teacherService.findById(idTeacher);

    if (teacher == null) {
      return new ResponseEntity(new CustomErrorType("No existe el teacher con el id enviado"),
          HttpStatus.NOT_FOUND);
    }

    try {
      String fileName = teacher.getAvatar();
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

}
