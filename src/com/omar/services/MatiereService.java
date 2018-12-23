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
import com.omar.entities.Matiere;
import com.omar.jwtauth.JWTTokenNeeded;

@JWTTokenNeeded
@Path("/matieres")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class MatiereService {

	@EJB
	private MatiereDaoLocal matiereDao;

	@EJB
	private EnseignantDaoLocal enseignantDao;

	@EJB
	private TacheDaoLocal tacheDao;

	private ObjectMapper mapper = new ObjectMapper();

	@GET
	public Response getAll() {

		List<Matiere> t = matiereDao.getAll();

		return Response.ok().entity(t).build();
	}

	@GET
	@Path("/{id}")
	public Response getByID(@PathParam("id") Long id) {

		Optional<Matiere> e = matiereDao.get(id);

		if (e.isPresent()) {
			return Response.ok().entity(e.get()).build();
		}

		JsonNode response = mapper.createObjectNode();

		((ObjectNode) response).put("message", "Could not find task with id: " + id);

		return Response.status(Response.Status.NOT_FOUND).entity(response).build();
	}

	@POST
	public Response add(Matiere tache) {

		JsonNode response = mapper.createObjectNode();

		try {

			matiereDao.save(tache);

		} catch (Exception e) {

			e.printStackTrace();
			((ObjectNode) response).put("message", e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
		}

		((ObjectNode) response).put("message", "Subject created successfully");

		return Response.ok().entity(response).build();
	}

	@PUT
	@Path("/{id}")
	public Response edit(@PathParam("id") Long id, Matiere tache) {

		Optional<Matiere> toEdit;
		JsonNode response = mapper.createObjectNode();

		if ((toEdit = matiereDao.get(id)).isPresent()) {

			try {
				matiereDao.update(toEdit.get(), tache);
			} catch (Exception e) {
				e.printStackTrace();
				((ObjectNode) response).put("message", e.getMessage());
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
			}

			((ObjectNode) response).put("message", "Subject " + id + " was edited successfully");
			return Response.ok().entity(response).build();

		} else {
			((ObjectNode) response).put("message", "Subject " + id + " not found");
			return Response.status(Response.Status.NOT_FOUND).entity(response).build();
		}
	}

	@DELETE
	@Path("/{id}")
	public Response delete(@PathParam("id") Long id) {

		Optional<Matiere> toDelete;
		JsonNode response = mapper.createObjectNode();

		if ((toDelete = matiereDao.get(id)).isPresent()) {

			try {
				matiereDao.delete(toDelete.get());
			} catch (Exception e) {
				e.printStackTrace();
				((ObjectNode) response).put("message", e.getMessage());
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
			}

			((ObjectNode) response).put("message", "Subject " + id + " was deleted successfully");
			return Response.ok().entity(response).build();

		} else {
			((ObjectNode) response).put("message", "Subject " + id + " not found");
			return Response.status(Response.Status.NOT_FOUND).entity(response).build();
		}

	}
}
