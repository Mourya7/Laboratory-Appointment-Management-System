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
      List<Object> objs = dbSingleton.db.getData("Appointment", "");
      
      if(objs.isEmpty()) {
       initialize();
       objs = dbSingleton.db.getData("Appointment", "");
      }
            
      return xmlMarshalling(objs);
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
            
            Element name = doc.createElement("name");
            name.appendChild(doc.createTextNode(patientObj.getName()));
            patient.appendChild(name);
            
            Element address = doc.createElement("address");
            address.appendChild(doc.createTextNode(patientObj.getAddress()));
            patient.appendChild(address); 
            
            Element insurance = doc.createElement("insurance");
            insurance.appendChild(doc.createTextNode(String.valueOf(patientObj.getInsurance())));
            patient.appendChild(insurance);
            
            Element dob = doc.createElement("dob");
            dob.appendChild(doc.createTextNode(patientObj.getDateofbirth().toString()));
            patient.appendChild(dob);
            
            Phlebotomist phlebotomistObj = appointmentObj.getPhlebid();
            Element phlebotomist = doc.createElement("phlebotomist");
            phlebotomist.setAttribute("id",phlebotomistObj.getId());
            appointment.appendChild(phlebotomist);
             
            Element phlebotomistName = doc.createElement("name");
            phlebotomistName.appendChild(doc.createTextNode(phlebotomistObj.getName()));
            phlebotomist.appendChild(phlebotomistName);

            PSC pscObj = appointmentObj.getPscid();
            Element psc = doc.createElement("psc");
            psc.setAttribute("id",pscObj.getId());
            appointment.appendChild(psc);
             
            Element pscName = doc.createElement("name");
            pscName.appendChild(doc.createTextNode(pscObj.getName()));
            psc.appendChild(pscName);
            
            List<AppointmentLabTest> labTests = appointmentObj.getAppointmentLabTestCollection();
            
            Element allLabTests = doc.createElement("allLabTests");
            appointment.appendChild(allLabTests);
            
            for(AppointmentLabTest labTest : labTests){
               Element appointmentLabTest = doc.createElement("appointmentLabTest");
               allLabTests.appendChild(appointmentLabTest);
               appointmentLabTest.setAttribute("appointmentId",labTest.getAppointment().getId());
               appointmentLabTest.setAttribute("dxcode",labTest.getAppointmentLabTestPK().getDxcode());
               appointmentLabTest.setAttribute("labTestId",labTest.getLabTest().getId());
            }             
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
       System.out.println(a.getAllAppointments());
       System.out.println("--------");
       System.out.println(a.getAppointment("770"));  
   }
}