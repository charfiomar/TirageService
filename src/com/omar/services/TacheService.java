package com.omar.services;

import java.util.List;
import java.util.Optional;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.omar.daos.EnseignantDaoLocal;
import com.omar.daos.MatiereDaoLocal;
import com.omar.daos.TacheDaoLocal;
import com.omar.entities.Enseignant;
import com.omar.entities.Matiere;
import com.omar.entities.Tache;
import com.omar.jwtauth.JWTTokenNeeded;

@JWTTokenNeeded
@Path("/taches")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class TacheService {
	
	@EJB
	private TacheDaoLocal tacheDao;
	
	@EJB
	private EnseignantDaoLocal enseignantDao;
	
	@EJB
	private MatiereDaoLocal matiereDao;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@GET
	public Response getAll() {

		List<Tache> t = tacheDao.getAll();

		return Response.ok().entity(t).build();
	}
	
	@GET
	@Path("/{id}")
	public Response getByID(@PathParam("id") Long id) {

		Optional<Tache> e = tacheDao.get(id);

		if (e.isPresent()) {
			return Response.ok().entity(e.get()).build();
		}

		JsonNode response = mapper.createObjectNode();

		((ObjectNode) response).put("message", "Could not find task with id: " + id);

		return Response.status(Response.Status.NOT_FOUND).entity(response).build();
	}
	
	@POST
	@Path("/enseignant/{idE}/matiere/{idM}")
	public Response add(Tache tache, @PathParam("idE") Long idEnseignant, @PathParam("idM") Long idMatiere) {

		JsonNode response = mapper.createObjectNode();
		
		try {
			Optional<Matiere> matiere = matiereDao.get(idMatiere);
			Optional<Enseignant> enseignant = enseignantDao.get(idEnseignant);
			
			if(!enseignant.isPresent()){
				((ObjectNode) response).put("message", "Enseignant not found");
				return Response.status(Response.Status.NOT_FOUND).entity(response).build();
			}
			if(!matiere.isPresent()){
				((ObjectNode) response).put("message", "Matiere not found");
				return Response.status(Response.Status.NOT_FOUND).entity(response).build();
			}
			
			tache.setMatiere(matiere.get());
			tache.setEnseignant(enseignant.get());
			tacheDao.save(tache);

		} catch (Exception e) {

			e.printStackTrace();
			((ObjectNode) response).put("message", e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
		}

		((ObjectNode) response).put("message", "Task created successfully");

		return Response.ok().entity(response).build();
	}

	@PUT
	@Path("/{id}/enseignant/{idE}/matiere/{idM}")
	public Response edit(@PathParam("id") Long id, Tache tache, @PathParam("idE") Long idEnseignant, @PathParam("idM") Long idMatiere) {

		Optional<Tache> toEdit;
		JsonNode response = mapper.createObjectNode();

		if ((toEdit = tacheDao.get(id)).isPresent()) {

			try {
				
				Optional<Matiere> matiere = matiereDao.get(idMatiere);
				Optional<Enseignant> enseignant = enseignantDao.get(idEnseignant);
				
				if(!enseignant.isPresent()){
					((ObjectNode) response).put("message", "Enseignant not found");
					return Response.status(Response.Status.NOT_FOUND).entity(response).build();
				}
				if(!matiere.isPresent()){
					((ObjectNode) response).put("message", "Matiere not found");
					return Response.status(Response.Status.NOT_FOUND).entity(response).build();
				}
				
				tache.setMatiere(matiere.get());
				tache.setEnseignant(enseignant.get());
								
				tacheDao.update(toEdit.get(), tache);
				
			} catch (Exception e) {
				e.printStackTrace();
				((ObjectNode) response).put("message", e.getMessage());
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
			}

			((ObjectNode) response).put("message", "Task " + id + " was edited successfully");
			return Response.ok().entity(response).build();

		} else {
			((ObjectNode) response).put("message", "Task " + id + " not found");
			return Response.status(Response.Status.NOT_FOUND).entity(response).build();
		}
	}

	@DELETE
	@Path("/{id}")
	public Response delete(@PathParam("id") Long id) {

		Optional<Tache> toDelete;
		JsonNode response = mapper.createObjectNode();

		if ((toDelete = tacheDao.get(id)).isPresent()) {

			try {
				tacheDao.delete(toDelete.get());
			} catch (Exception e) {
				e.printStackTrace();
				((ObjectNode) response).put("message", e.getMessage());
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
			}

			((ObjectNode) response).put("message", "Task " + id + " was deleted successfully");
			return Response.ok().entity(response).build();

		} else {
			((ObjectNode) response).put("message", "Task " + id + " not found");
			return Response.status(Response.Status.NOT_FOUND).entity(response).build();
		}

	}
}
