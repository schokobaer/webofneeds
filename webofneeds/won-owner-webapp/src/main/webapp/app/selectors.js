/**
 * Created by ksinger on 22.01.2016.
 */

import { createSelector } from 'reselect';
import Immutable from 'immutable';
import won from './won-es6';
import {
    decodeUriComponentProperly,
    is,
} from './utils';

import {
    selectTimestamp,
} from './won-utils';

export const selectConnections = state => state.getIn(['connections']);
export const selectEvents = state => state.getIn(['events', 'events']);
export const selectLastUpdateTime = state => state.get('lastUpdateTime');
export const selectRouterParams = state => state.getIn(['router', 'currentParams']);

export const selectAllNeeds = state => state.get("needs");
export const selectAllOwnNeeds = state => selectAllNeeds(state).filter(need =>
    need.get("ownNeed")
);
export const selectAllTheirNeeds = state => selectAllNeeds(state).filter(need =>
    !need.get("ownNeed")
);

/**
 * Get the need for a given connectionUri
 * @param connectionUri
 */
export function selectNeedByConnectionUri(state, connectionUri){
    let needs = selectAllOwnNeeds(state); //we only check own needs as these are the only ones who have connections stored
    return needs.filter(need => need.getIn(["connections", connectionUri])).first();
}

/**
 * Get all connections stored within your own needs as a map
 * @returns Immutable.Map with all connections
 */
export function selectAllConnections(state) {
    const needs = selectAllOwnNeeds(state); //we only check own needs as these are the only ones who have connections stored
    let connections = Immutable.Map();

    needs.map(function(need){
        connections = connections.merge(need.get("connections"));
    });

    return connections;
}

export function selectAllMessages(state) {
    const connections = selectAllConnections(state);
    let messages = Immutable.Map();

    connections.map(function(conn){
        messages = messages.merge(conn.get("messages"));
    });

    return messages;
}

export function selectAllMessagesByNeedUri(state, needUri) {
    const connections = state.getIn(["needs", needUri, "connections"]);
    let messages = Immutable.Map();

    if(connections){
        connections.map(function(conn){
            messages = messages.merge(conn.get("messages"));
        });
    }

    return messages;
}

export const selectUnreadEventUris = state => state
    .getIn(['events', 'unreadEventUris']);

export const selectRemoteEvents = createSelector(
    selectEvents,
    events => {
        const remoteUrisAndEvents = events
            .toList()
            .map(e => {
                let remote = e.get('hasCorrespondingRemoteMessage') // select remote
                if(is('String', remote)) remote = events.get(remote); // for those rare cases where remote is only a uri
                if(!remote) return undefined;
                remote = remote.set('correspondsToOwnMsg', e); //add back-reference to it
                return remote && [remote.get('uri'), remote]
            })
            .filter(uriAndEvent => uriAndEvent); // filter out `undefined`s
        return Immutable.Map(remoteUrisAndEvents)
    }
)

export const selectUnreadEvents = createSelector(
    selectEvents,
    selectUnreadEventUris,
    (events, unreadEventUris) =>
        unreadEventUris.map(eventUri => events.get(eventUri))
);

//const selectUnreadEvents = state => state.getIn(['events', 'unreadEventUris']);

/**
 * @param {object} state
 * @return {object} events grouped by need.
 *      `unreadEventsByNeed.get(needUri)`, e.g.:
 *      `unreadEventsByNeed.get('http://example.org/won/resource/need/1234')`
 */
export const selectUnreadEventsByNeed = createSelector(
    selectUnreadEvents, selectConnections,
    // group by need, resulting in:  `{ <needUri>: { <cnctUri>: e1, <cnctUri>: e2, ...}, <needUri>: ...}`
    //TODO hasReceiverNeed is not guaranteed to exist.
    (unreadEvents, connections) => unreadEvents.groupBy(e => {
        const connectionUri = e.get('hasReceiver');
        return connections.getIn([connectionUri, 'belongsToNeed']);
    })
);

/**
 * from: state.events.unreadEventUris  of "type" ~Map<connection,latestevent>
 * to: ~Map<receiverneeduri, Map<connection,latestevent>>, e.g.:
 *     { <needUri>: { <eventType> : { <cnctUri>: e1, <cnctUri>: e2, ...}, <eventType> :... }, <needUri>: ...}
 *
 * access events as `const event = groupedEvents.getIn([needUri, eventType, cnctUri])`
 *
 * @param {object} state
 * @return {object} events grouped primarily by need and secondarily by type
 */
export const selectUnreadEventsByNeedAndType = createSelector(
    selectUnreadEventsByNeed,
    eventsGroupedByNeed =>
        // group by event-type
        eventsGroupedByNeed.map(groupByType)
);

function groupByType(events) {
    return events.groupBy(e =>
        e.get('hasMessageType') ||
        e.getIn(['hasCorrespondingRemoteMessage', 'hasMessageType'])
    )
}

export const selectUnreadEventsByConnectionAndType = createSelector(
    selectUnreadEvents,
    unreadEvents => unreadEvents
        .groupBy(e =>  e.get('hasReceiver')) // we're only interested in new message received (we know the ones we send)
        .map(groupByType)
);

/**
 * @param {object} state
 * @return {object} event counts for each connection. access via
 *      `unreadCounts.getIn([cnctUri, eventType])`, e.g.:
 *      `unreadCounts.getIn(['http://example.org/won/resource/connection/1234', won.EVENT.HINT_RECEIVED])`
 */
export const selectUnreadCountsByConnectionAndType = createSelector(
    selectUnreadEventsByConnectionAndType,
    unreadEvents => unreadEvents.map(eventsByType => eventsByType.map(events => events.size))
);

/**
 * @param {object} state
 * @return {object} event counts for each need. access via
 *      `unreadCounts.getIn([needUri, eventType])`, e.g.:
 *      `unreadCounts.getIn(['http://example.org/won/resource/need/1234', won.EVENT.HINT_RECEIVED])`
 */
export const selectUnreadCountsByNeedAndType = createSelector(
    selectUnreadEventsByNeedAndType,
    unreadEventsByNeedAndType =>
        unreadEventsByNeedAndType.map(eventsByType => //looking at single need's events grouped by type
            eventsByType.map(evnts => evnts.size) // looking at specific need and type -> just count now
        )

);

/**
 * @param {object} state
 * @return {object} event counts for each event type. access via
 *      `unreadCountsByType.get(eventType)`, e.g.:
 *      `unreadCountsByType.getIn(won.EVENT.HINT_RECEIVED)`
 */
export const selectUnreadCountsByType = createSelector(
    selectUnreadEvents,
    unreadEvents => groupByType(unreadEvents)
        .map(eventsOfType => eventsOfType.size)
);

/**
 * selects a map of `connectionUri -> { connection, events, ownNeed, remoteNeed }`
 * - thus: everything a connection has direct references to. Use this selector
 * when you're needing connection-centric data (e.g. for a view with a strong
 * focus on the connection)
 *
 * NOTE: the app-state used to have events and needs stored in this fashion.
 * Thus, this selector is also used to allow older code to use the new
 * state-structure with minimal changes.
 */
export const selectAllByConnections = createSelector(
    state => state, //reselect's createSelector always needs a dependency
    state => state
        .getIn(['connections'])
        .map(connection => allByConnection(connection)(state))
);
const allByConnection = (connection) => (state) => {
    const ownNeedUri = connection.get('belongsToNeed');
    const ownNeed = state.getIn(['needs', ownNeedUri]);

    const remoteNeedUri = connection.get('hasRemoteNeed');
    const remoteNeed = state.getIn(['needs', remoteNeedUri]);

    const events = connection
        .get('hasEvents')
        .map(eventUri => state.getIn(['events', 'events', eventUri]))
        .filter(event => !!event);


    return Immutable.Map({ connection, events, ownNeed, remoteNeed });
};

export const selectOpenConnectionUri = createSelector(
    selectRouterParams,
    selectConnections,
    (routerParams, connections) => {
        //de-escaping is lost in transpiling if not done in two steps :|
        const openConnectionUri = decodeUriComponentProperly(
            routerParams.get('connectionUri') ||
            routerParams.get('openConversation')
        );

        const myUri = decodeUriComponentProperly(routerParams.get('myUri')); //TODO deprecated parameter

        const theirUri = decodeUriComponentProperly(routerParams.get('theirUri')); //TODO deprecated parameter

        if(openConnectionUri) {
            return openConnectionUri;
        } else if (myUri && theirUri) {
            /*
             returns undefined when there's no
             connection like that in the state.
             */
            return connections
                .filter(c =>
                    c.get('belongsToNeed') === myUri  &&
                    c.get('hasRemoteNeed') === theirUri
                ).keySeq().first()
        } else {
            return undefined;
        }
    }
);

export const selectOpenConnection = createSelector(
    selectOpenConnectionUri, selectConnections,
    (uri, connections) =>
        connections.get(uri)
);

export const selectEventsOfOpenConnection = createSelector(
    selectOpenConnection, selectEvents,
    (connection, allEvents) => connection && connection
        .get('hasEvents')
        .map(eventUri => allEvents.get(eventUri))
);

export const selectConnectMessageOfOpenConnection = createSelector(
    selectEventsOfOpenConnection,
    events => events && events
        .filter(event => {
            if(!event) return;
            const messageType =
                event.getIn(['hasCorrespondingRemoteMessage', 'hasMessageType']) ||
                event.get('hasMessageType') === won.WONMSG.connectMessage;
            return messageType === won.WONMSG.connectMessage ||
                messageType === won.WONMSG.connectMessageCompacted;
        })
        .first()
);

export const selectOpenPostUri = createSelector(
    state => state,
    state => {
        const encodedPostUri =
            state.getIn(['router', 'currentParams', 'postUri']) ||
            state.getIn(['router', 'currentParams', 'myUri']); //deprecated parameter
        return decodeUriComponentProperly(encodedPostUri);
    }
);

export const displayingOverview = createSelector(
    selectOpenPostUri,
    postUri => !postUri //if there's a postUri, this is almost certainly a detail view
);

/**
 * @deprecated doesn't use daisy-chaining yet.
 */
export const selectLastUpdatedPerConnection = createSelector(
    selectAllByConnections,
    allByConnections => allByConnections.map(connectionAndRelated =>
        connectionAndRelated.get('events')
        .map( event =>
            //selectTimestamp(event, connectionAndRelated.getIn(['connection','uri']) )
            selectTimestamp(event)
        )
        /*
         * don't use events without timestamp
         * NOTE if there's no events with timestamps
         * for the connection:
         * `Immutable.List([]).max() === undefined`
         */
        .filter(timestamp => timestamp)
        .map(timestamp => Number.parseInt(timestamp))
        .max()
    )
);