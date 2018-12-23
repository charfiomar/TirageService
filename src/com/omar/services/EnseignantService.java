package com.omar.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.omar.daos.TacheDaoLocal;
import com.omar.entities.Enseignant;
import com.omar.entities.Tache;
import com.omar.jwtauth.JWTTokenNeeded;

@JWTTokenNeeded
@Path("/enseignants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class EnseignantService {

	@EJB
	private EnseignantDaoLocal enseignantDao;

	@EJB
	private TacheDaoLocal tacheDao;

	private ObjectMapper mapper = new ObjectMapper();

	@GET
	public Response getAll() {

		List<Enseignant> e = enseignantDao.getAll();

		return Response.ok().entity(e).build();
	}

	@GET
	@Path("/{id}")
	public Response getByID(@PathParam("id") Long id) {

		Optional<Enseignant> e = enseignantDao.get(id);

		if (e.isPresent()) {
			return Response.ok().entity(e.get()).build();
		}

		JsonNode response = mapper.createObjectNode();

		((ObjectNode) response).put("message", "Could not find enseignant with id: " + id);

		return Response.status(Response.Status.NOT_FOUND).entity(response).build();
	}

	@POST
	public Response add(Enseignant enseignant) {

		JsonNode response = mapper.createObjectNode();

		try {

			enseignantDao.save(enseignant);

		} catch (Exception e) {

			e.printStackTrace();
			((ObjectNode) response).put("message", e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
		}

		((ObjectNode) response).put("message", "Enseignant created successfully");

		return Response.ok().entity(response).build();
	}

	@PUT
	@Path("/{id}")
	public Response edit(@PathParam("id") Long id, Enseignant enseignant) {

		Optional<Enseignant> toEdit;
		JsonNode response = mapper.createObjectNode();

		if ((toEdit = enseignantDao.get(id)).isPresent()) {

			try {
				enseignantDao.update(toEdit.get(), enseignant);
			} catch (Exception e) {
				e.printStackTrace();
				((ObjectNode) response).put("message", e.getMessage());
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
			}

			((ObjectNode) response).put("message", "Enseignant " + id + " was edited successfully");
			return Response.ok().entity(response).build();

		} else {
			((ObjectNode) response).put("message", "Enseignant " + id + " not found");
			return Response.status(Response.Status.NOT_FOUND).entity(response).build();
		}
	}

	@DELETE
	@Path("/{id}")
	public Response delete(@PathParam("id") Long id) {

		Optional<Enseignant> toDelete;
		JsonNode response = mapper.createObjectNode();

		if ((toDelete = enseignantDao.get(id)).isPresent()) {

			try {
				enseignantDao.delete(toDelete.get());
			} catch (Exception e) {
				e.printStackTrace();
				((ObjectNode) response).put("message", e.getMessage());
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
			}

			((ObjectNode) response).put("message", "Enseignant " + id + " was deleted successfully");
			return Response.ok().entity(response).build();

		} else {
			((ObjectNode) response).put("message", "Enseignant " + id + " not found");
			return Response.status(Response.Status.NOT_FOUND).entity(response).build();
		}

	}

	@GET
	@Path("/{id}/matieres")
	public Response getMatieres(@PathParam("id") Long id) {

		Optional<Enseignant> e = enseignantDao.get(id);

		if (e.isPresent()) {
			return Response.ok().entity(e.get().getMatieres()).build();
		}

		JsonNode response = mapper.createObjectNode();

		((ObjectNode) response).put("message", "Could not find enseignant with id: " + id);

		return Response.status(Response.Status.NOT_FOUND).entity(response).build();
	}

	@GET
	@Path("/{id}/taches")
	public Response getTaches(@PathParam("id") Long id) {

		Optional<Enseignant> e = enseignantDao.get(id);

		if (e.isPresent()) {
						
			List<Tache> taches = tacheDao.getAll().stream().filter(t -> t.getEnseignant().equals(e.get()))
					.collect(Collectors.toList());

			return Response.ok().entity(taches).build();
		}

		JsonNode response = mapper.createObjectNode();

		((ObjectNode) response).put("message", "Could not find enseignant with id: " + id);

		return Response.status(Response.Status.NOT_FOUND).entity(response).build();
	}
}
