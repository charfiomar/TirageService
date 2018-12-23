package com.omar.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.omar.jwtauth.JWTTokenNeeded;

@Path("/document")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Stateless
public class DocumentService {

	private final String UPLOADED_FILE_PATH = "/home/omar/EclipseSpringWorkSpace/TirageService/WebContent/resources/";
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@GET
	@Path("/download/{filename}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response download(@PathParam("filename") String fileName) throws FileNotFoundException{
				
		final InputStream input = new FileInputStream(new File(UPLOADED_FILE_PATH + fileName));
		
		StreamingOutput stream = new StreamingOutput() {
			
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				try {
	                output.write(IOUtils.toByteArray(input));
	            }
	            catch (Exception e) {
	                throw new WebApplicationException(e);
	            }
			}
		};
		
		return Response.ok(stream, MediaType.APPLICATION_OCTET_STREAM).header("content-disposition", "attachment; filename=\""+ fileName +"\"").build();
	}
	
	@POST
	@Path("/upload")
	@JWTTokenNeeded
	public Response upload(MultipartFormDataInput input) {
		
		JsonNode response = mapper.createObjectNode();
		String fileName = "";
		
		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		List<InputPart> inputParts = uploadForm.get("file");

		for (InputPart inputPart : inputParts) {

			try {

				MultivaluedMap<String, String> header = inputPart.getHeaders();
				
				fileName = new Date().getTime() + getFileName(header);

				InputStream inputStream = inputPart.getBody(InputStream.class, null);

				byte[] bytes = IOUtils.toByteArray(inputStream);

				String filePath = UPLOADED_FILE_PATH + fileName;

				writeFile(bytes, filePath);

			} catch (IOException e) {
				e.printStackTrace();
				((ObjectNode) response).put("message", e.getMessage());
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
			}

		}
		((ObjectNode) response).put("filename", fileName);
		((ObjectNode) response).put("message", "file uploaded successfully");
		return Response.ok().entity(response).build();
	}

	private String getFileName(MultivaluedMap<String, String> header) {

		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

		for (String filename : contentDisposition) {
			if ((filename.trim().startsWith("filename"))) {

				String[] name = filename.split("=");

				String finalFileName = name[1].trim().replaceAll("\"", "");
				return finalFileName;
			}
		}
		return "unknown";
	}

	private void writeFile(byte[] content, String filename) throws IOException {

		File file = new File(filename);

		if (!file.exists()) {
			file.createNewFile();
		}

		FileOutputStream fop = new FileOutputStream(file);

		fop.write(content);
		fop.flush();
		fop.close();

	}
}
