package ru.mail.polis.service;

import one.nio.http.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.mail.polis.DAO;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

import static ru.mail.polis.service.ServiceUtils.*;

public class ServiceImpl extends HttpServer implements Service {

    @NotNull
    private final DAO dao;

    public ServiceImpl(@NotNull DAO dao, int port) throws IOException {
        super(configFrom(port));
        this.dao = dao;
    }

    @NotNull
    @Path("/v0/entity")
    @RequestMethod(Request.METHOD_PUT)
    public Response put(@Param(value = "id", required = true) final String id, final Request request) {
        try {
            assertNotEmpty(id);
            dao.upsert(byteBufferFrom(id), byteBufferFrom(request.getBody()));
            return new Response(Response.CREATED, Response.EMPTY);
        } catch (IllegalArgumentException e) {
            logger.error("Required param must not be empty!");
            return new Response(Response.BAD_REQUEST, Response.EMPTY);
        } catch (IOException e) {
            logger.debug("Failed to get data {}", request.getBody(), e);
            return new Response(Response.INTERNAL_ERROR, Response.EMPTY);
        }
    }

    @NotNull
    @Path("/v0/entity")
    @RequestMethod(Request.METHOD_GET)
    public Response get(@Param(value = "id", required = true) final String id) {
        try {
            assertNotEmpty(id);
            final ByteBuffer value = dao.get(byteBufferFrom(id));
            return Response.ok(byteArrayFrom(value));
        } catch (IllegalArgumentException e) {
            logger.error("Required param must not be empty!");
            return new Response(Response.BAD_REQUEST, Response.EMPTY);
        } catch (NoSuchElementException e) {
            logger.error("Failed to find value by key {}", id);
            return new Response(Response.NOT_FOUND, Response.EMPTY);
        } catch (IOException e) {
            logger.error("Failed to get data {}", id, e);
            return new Response(Response.INTERNAL_ERROR, Response.EMPTY);
        }
    }

    @NotNull
    @Path("/v0/entity")
    @RequestMethod(Request.METHOD_DELETE)
    public Response delete(@Param(value = "id", required = true) final String id) {
        try {
            assertNotEmpty(id);
            dao.remove(byteBufferFrom(id));
            return new Response(Response.ACCEPTED, Response.EMPTY);
        } catch (IllegalArgumentException e) {
            logger.error("Required param must not be empty!");
            return new Response(Response.BAD_REQUEST, Response.EMPTY);
        } catch (IOException e) {
            logger.debug("Failed to delete {}", id, e);
            return new Response(Response.INTERNAL_ERROR, Response.EMPTY);
        }
    }

    @NotNull
    @Path("/v0/status")
    public Response status() {
        return Response.ok(Response.OK);
    }

    @Override
    public void handleDefault(@Nullable final Request request, @NotNull final HttpSession session) throws IOException {
        logger.error("Incorrect request {}", request);
        session.sendResponse(new Response(Response.BAD_REQUEST, Response.EMPTY));
    }
}
