/**
 * Created by ksinger on 24.08.2015.
 */
;

import angular from 'angular';
import visitorTitleBarModule from '../owner-title-bar';
import galleryModule from '../gallery';
import postMessagesModule from '../post-messages';
import { attach,mapToMatches } from '../../utils';
import won from '../../won-es6';
import { actionCreators }  from '../../actions/actions';
import openConversationModule from '../open-conversation';
import connectionSelectionModule from '../connection-selection';
import { selectAllByConnections } from '../../selectors';

const serviceDependencies = ['$q', '$ngRedux', '$scope'];
class Controller {
    constructor() {
        attach(this, serviceDependencies, arguments);
        this.selection = 0;
        window.msgs4dbg = this;

        const selectFromState = (state)=>{

            const myNeedUri = decodeURIComponent(state.getIn(
                ['router', 'currentParams', 'myUri']));

            const connectionUrisOfNeed = state.getIn(
                ['needs', 'ownNeeds', myNeedUri, 'hasConnections']);

            const conversations = connectionUrisOfNeed
                .map(connectionUri =>
                    state.getIn(['connections', connectionUri]))
                .filter(conn =>
                    conn.get('hasConnectionState') === won.WON.Connected
                );

            const conversationUris = conversations.map(c => c.get('uri'));

            const post = state.getIn(['needs','ownNeeds', myNeedUri]);

            return {
                connectionType: won.WON.Connected,
                post: post && post.toJS? post.toJS() : {},
                allByConnections: selectAllByConnections(state).toJS(), //TODO plz don't do `.toJS()`. every time an ng-binding somewhere cries.
                conversations: conversations.toList().toJS(),//TODO plz don't do `.toJS()`. every time an ng-binding somewhere cries.
                conversationUris: conversationUris.toJS(), //TODO plz don't do `.toJS()`. every time an ng-binding somewhere cries.
                routerParams: state.getIn(['router', 'currentParams']),
            };
        }

        const disconnect = this.$ngRedux.connect(selectFromState, actionCreators)(this);
        this.$scope.$on('$destroy', disconnect);
    }
    openConversation(connectionUri) {
        console.log('openConversation ', connectionUri);
        this.router__stateGo('postConversations', {
            myUri: decodeURIComponent(this.routerParams.get('myUri')),
            openConversation: connectionUri,
        })
    }
}

Controller.$inject = serviceDependencies;

export default angular.module('won.owner.components.postOwner.messages', [
        visitorTitleBarModule,
        galleryModule,
        postMessagesModule,
        connectionSelectionModule,
    ])
    .controller('PostOwnerMessagesController', Controller)
    .name;
