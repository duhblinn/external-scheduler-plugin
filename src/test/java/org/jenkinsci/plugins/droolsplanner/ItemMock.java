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

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.labels.LabelAtom;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.powermock.reflect.Whitebox;

class ItemMock {

    public static List<Queue.BuildableItem> list() {

        return new ArrayList<Queue.BuildableItem>();
    }

    public static Queue.BuildableItem create(
            final Set<Node> nodes, int id, String displayName, long inQueueSince
    ) {

        final Queue.BuildableItem item = mock(Queue.BuildableItem.class);
        when(item.getAssignedLabel()).thenReturn(new LabelAtom ("Label name") {
            @Override
            public Set<Node> getNodes() {

                return nodes;
            }
        });

        Whitebox.setInternalState(item, "id", id);
        when(item.getInQueueSince()).thenReturn(inQueueSince);

        final Queue.Task task = mock(Queue.Task.class);
        when(task.getDisplayName()).thenReturn(displayName);

        Whitebox.setInternalState(item, "task", task);

        return item;
    }
}