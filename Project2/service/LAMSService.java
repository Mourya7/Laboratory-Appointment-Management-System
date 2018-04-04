package service;

import java.util.*;
import components.data.*;
import business.*;

import java.io.StringWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class LAMSService {

   private DBSingleton dbSingleton;
   
   public String initialize() {
      dbSingleton = DBSingleton.getInstance();       
      dbSingleton.db.initialLoad("LAMS");      
      return "Database Initialized";
   }
   
   public String getAllAppointments() {
      Object singletonObject;
      String allAppointment = ""; 
      List<Object> objs = dbSingleton.db.getData("Appointment", "");
      
      if(objs.isEmpty()) {
       initialize();
       objs = dbSingleton.db.getData("Appointment", "");
      }
      
      for (Object obj : objs){
         singletonObject = obj;
         allAppointment += singletonObject.toString();
      }
      return allAppointment;
   }
   
   public String addAppointment(String xml) {
      return " ";
   }
   
   public String getAppointment(String appointmentId) {
      Object singletonObject = null;
      String allAppointments  = "";
      String whereClause = "id = '"+appointmentId+"'";
      List<Object> objs = dbSingleton.db.getData("Appointment",whereClause);
      
      if(objs.isEmpty()) {
       initialize();
       objs = dbSingleton.db.getData("Appointment",whereClause);
      }
      
      allAppointments = xmlMarshalling(objs);
      
      if(allAppointments.isEmpty()) {
         return "Appointment doesn't exist";
      }else {
         return allAppointments;
      }
   }
   
   public String xmlMarshalling(List<Object> objs) {
      try {
         DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
         
         Document doc = docBuilder.newDocument();
         Element appointmentList = doc.createElement("AppointmentList");
         doc.appendChild(appointmentList);
         
         for (Object obj : objs){
            Appointment appointmentObj = (Appointment)obj;
            Element appointment = doc.createElement("Appointment");
            appointmentList.appendChild(appointment);
            
            Attr date = doc.createAttribute("date");
            date.setValue(appointmentObj.getApptdate().toString());
            
            Attr id = doc.createAttribute("id");
            id.setValue(appointmentObj.getId());
            
            Attr time = doc.createAttribute("time");
            time.setValue(appointmentObj.getAppttime().toString());
            
            appointment.setAttributeNode(date);
            appointment.setAttributeNode(id);
            appointment.setAttributeNode(time);
            
            Patient patientObj = appointmentObj.getPatientid();
            
            Element patient = doc.createElement("patient");
            patient.setAttribute("id",patientObj.getId());
            
            appointment.appendChild(patient); 
          }         
         
         DOMSource source = new DOMSource(doc);
         
         StringWriter writer = new StringWriter();
         StreamResult result = new StreamResult(writer);
         
         TransformerFactory tFactory = TransformerFactory.newInstance();
         Transformer transformer = tFactory.newTransformer();
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
         transformer.transform(source,result);
         String strResult = writer.toString();
         
         return strResult;
         } catch (ParserConfigurationException pce) {
		   pce.printStackTrace();
	      }
          catch (TransformerException tfe) {
		   tfe.printStackTrace();
	      }
      return "";
   }
   
   public static void main(String args[]) {
       LAMSService a = new LAMSService();
       a.initialize();
       //System.out.println(a.getAllAppointments());
       //System.out.println("--------");
       System.out.println(a.getAppointment("770"));  
   }
}