package io.github.chronosx88.influence.kademlia.operation;

import java.io.IOException;
import io.github.chronosx88.influence.kademlia.exceptions.RoutingException;

/**
 * An operation in the Kademlia routing protocol
 *
 * @author Joshua Kissoon
 * @created 20140218
 */
public interface Operation
{

    /**
     * Starts an operation and returns when the operation is finished
     *
     * @throws RoutingException
     */
    public void execute() throws IOException, RoutingException;
}
