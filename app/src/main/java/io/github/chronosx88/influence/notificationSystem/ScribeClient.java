/*
 * Copyright (C) 2019 ChronosX88
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.chronosx88.influence.notificationSystem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.chronosx88.influence.helpers.AppHelper;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.scribe.Scribe;
import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.ScribeImpl;
import rice.p2p.scribe.Topic;
import rice.pastry.commonapi.PastryIdFactory;

public class ScribeClient implements rice.p2p.scribe.ScribeClient {
    private Map<String, Topic> topicMap = new ConcurrentHashMap<>();
    private Map<Topic, NotificationHandler> notificationHandlerMap = new ConcurrentHashMap<>();
    private Scribe scribe = new ScribeImpl(AppHelper.getPastryNode(), "scribeInstance");

    @Override
    public boolean anycast(Topic topic, ScribeContent scribeContent) {
        // We don't need anycast. Therefore, just suspend the wave.
        return true;
    }

    @Override
    public void deliver(Topic topic, ScribeContent scribeContent) {
        if(notificationHandlerMap.containsKey(topic)) {
            notificationHandlerMap.get(topic).handleNotification(scribeContent);
        }
    }

    @Override
    public void childAdded(Topic topic, NodeHandle nodeHandle) {
        // Nothing to do
    }

    @Override
    public void childRemoved(Topic topic, NodeHandle nodeHandle) {
        // Nothing to do
    }

    @Override
    public void subscribeFailed(Topic topic) {
        // Nothing to do
    }

    public void subscribeToTopic(String topicName, NotificationHandler handler) {
        Topic topic = new Topic(new PastryIdFactory(AppHelper.getPastryEnvironment()), topicName);
        scribe.subscribe(topic, this);
        topicMap.put(topicName, topic);
        notificationHandlerMap.put(topic, handler);
    }

    public void unsubscribeFromTopic(String topicName) {
        if(topicMap.containsKey(topicName)) {
            scribe.unsubscribe(topicMap.get(topicName), this);
            notificationHandlerMap.remove(topicMap.get(topicName));
        }
    }

    public void publishToTopic(String topicName, ScribeContent content) {
        Topic topic = new Topic(new PastryIdFactory(AppHelper.getPastryEnvironment()), topicName);
        scribe.publish(topic, content);
    }
}
