/*
 * The MIT License
 *
 * Copyright (c) 2012 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.droolsplanner;

import static org.junit.Assert.assertEquals;
import hudson.model.Action;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.labels.LabelAtom;
import hudson.model.queue.CauseOfBlockage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Node.class)
public class QueueSerializerServiceTest {

    private static final QueueSerializerService SERIALIZER = new QueueSerializerService();

    final Map<Integer, String> assignments = new HashMap<Integer, String>();

    @Test
    public void deserializeSingleItem() {

        final String json = "{\"solution\" : [ { \"id\" : 1, \"name\" : \"job@1\", \"node\" : \"vmg77-Win2k3-x86_64\" }," +
        		"{ \"id\" : 2, \"name\" : \"job@2\", \"node\" : \"not-assigned\" } ] }"
        ;

        final NodeAssignements assignments = SERIALIZER.deserialize(json);

        assertEquals(2, assignments.size());
        assertEquals("vmg77-Win2k3-x86_64", assignments.taskNodeName(1));
        assertEquals("not-assigned", assignments.taskNodeName(2));
    }

    @Test
    public void serializeSingleItem() {

        final String actual = SERIALIZER.serialize(singleItem(), NodeAssignements.builder().build());

        final String json = "{\"queue\":[{\"id\":2,\"priority\":50,\"inQueueSince\":3,\"name\":\"Single queue item\"," +
    		    "\"nodes\":[{\"name\":\"master\",\"executors\":2,\"freeExecutors\":1}],\"assigned\":null}]}"
        ;

        assertEquals(json, actual);
    }

    private List<Queue.Item> singleItem() {

        final List<Queue.Item> items = new ArrayList<Queue.Item>(1);
        final Set<Node> nodes = new HashSet<Node>(1);

        nodes.add(node("master", 2, 1));

        items.add(item(nodes, 2, "Single queue item", 3));

        return items;
    }

    @Test
    public void serializeSeveralItems() {

        final String actual = SERIALIZER.serialize(
                severalItems(),
                NodeAssignements.builder()
                        .assign(4, "slave2")
                        .build()
        );

        final String json = "{\"queue\":[{\"id\":2,\"priority\":50,\"inQueueSince\":3,\"name\":\"Single queue item\"," +
                "\"nodes\":[{\"name\":\"master\",\"executors\":2,\"freeExecutors\":1}],\"assigned\":null}," +
                "{\"id\":4,\"priority\":70,\"inQueueSince\":5,\"name\":\"raven_eap\"," +
                "\"nodes\":[{\"name\":\"slave1\",\"executors\":7,\"freeExecutors\":7},{\"name\":\"slave2\",\"executors\":1,\"freeExecutors\":0}]" +
                ",\"assigned\":\"slave2\"}]}"
        ;

        assertEquals(json, actual);
    }

    private List<Queue.Item> severalItems() {

        final List<Queue.Item> items = new ArrayList<Queue.Item>(1);

        SortedSet<Node> nodes = nodeList();

        nodes.add(node("master", 2, 1));

        items.add(item(nodes, 2, "Single queue item", 3));

        nodes = nodeList();

        nodes.add(node("slave1", 7, 7));
        nodes.add(node("slave2", 1, 0));

        items.add(item(nodes, 4, "raven_eap", 5));

        return items;
    }

    /**
     * Use sorted set simplify checking
     */
    private SortedSet<Node> nodeList () {

        return new TreeSet<Node>(new Comparator<Node>() {

            public int compare(Node o1, Node o2) {
System.out.println(o1.getDisplayName().compareTo(o2.getDisplayName()));
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });
    }

    private Node node(final String name, final int executors, final int freeExecutors) {

        final Computer computer = Mockito.mock(Computer.class);
        final Node node = PowerMockito.mock(Node.class);

        PowerMockito.when(node.getDisplayName()).thenReturn(name);
        PowerMockito.when(node.getNumExecutors()).thenReturn(executors);
        PowerMockito.when(node.toComputer()).thenReturn(computer);

        PowerMockito.when(computer.countIdle()).thenReturn(freeExecutors);

        return node;
    }

    private Queue.Item item(
            final Set<Node> nodes, int id, String displayName, int inQueueSince
    ) {

        final Queue.Task task = Mockito.mock(Queue.Task.class);

        Mockito.when(task.getDisplayName()).thenReturn(displayName);

        return new ItemMock(task, nodes, id, inQueueSince);
    }

    private static class ItemMock extends Queue.Item {

        private final Set<Node> nodes;

        public ItemMock(Queue.Task task, Set<Node> nodes, int id, int inQueueSince) {

            super(task, Collections.<Action>emptyList(), id, null, inQueueSince);

            this.nodes= nodes;
        }

        @Override
        public CauseOfBlockage getCauseOfBlockage() {

            throw new AssertionError("Noone is supposed to to call that");
        }

        public LabelAtom getAssignedLabel() {

            return new LabelAtom ("Label name") {
                @Override
                public Set<Node> getNodes() {

                    return nodes;
                }
            };
        }
    }
}
