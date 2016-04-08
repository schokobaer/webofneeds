;

import angular from 'angular';
import squareImageModule from '../components/square-image';
import { attach,mapToMatches } from '../utils';
import won from '../won-es6';
import { labels } from '../won-label-utils';
import { selectUnreadEventsByNeedAndType, selectAllByConnections } from '../selectors';
import { actionCreators }  from '../actions/actions';

const serviceDependencies = ['$q', '$ngRedux', '$scope'];
function genComponentConf() {
    let template = `
        <nav class="need-tab-bar" ng-cloak ng-show="{{true}}">
            <div class="ntb__inner">
                <div class="ntb__inner__left">
                    <a ui-sref="overviewPosts">
                        <img src="generated/icon-sprite.svg#ico36_backarrow" class="ntb__icon">
                    </a>
                    <won-square-image src="self.myNeed.get('titleImgSrc')" title="self.myNeed.get('title')"></won-square-image>
                    <div class="ntb__inner__left__titles">
                        <h1 class="ntb__title">{{self.myNeed.get('title')}}</h1>
                        <div class="ntb__inner__left__titles__type">{{self.labels.type[self.myNeed.get('basicNeedType')]}}</div>
                    </div>
                </div>
                <div class="ntb__inner__right">
                    <img class="ntb__icon clickable" src="generated/icon-sprite.svg#ico_settings" ng-show="!self.settingsOpen" ng-click="self.settingsOpen = true" ng-mouseenter="self.settingsOpen = true">
                    <button class="won-button--filled thin red" ng-show="self.isActive && self.settingsOpen" ng-mouseleave="self.settingsOpen=false" ng-click="self.closePost()">Close Post</button>
                    <button class="won-button--filled thin red" ng-show="!self.isActive && self.settingsOpen" ng-mouseleave="self.settingsOpen=false" ng-click="self.reOpenPost()">Reopen Post</button>
                    <ul class="ntb__tabs">
                        <li ng-class="{'ntb__tabs__selected' : self.selection == 4}">
                            <a ui-sref="postInfo({myUri: self.myNeedUri})">
                                Post Info
                            </a>
                        </li>
                        <li ng-class="{'ntb__tabs__selected' : self.selection == 0}">
                            <a ui-sref="postConversations({myUri: self.myNeedUri})"
                                ng-class="{'disabled' : !self.hasMessages}">
                                Messages
                                <span class="ntb__tabs__unread">{{ self.unreadMessages }}</span>
                            </a>
                        </li>
                        <li ng-class="{'ntb__tabs__selected' : self.selection == 1}">
                            <a ui-sref="overviewMatches({myUri: self.myNeedUri})"
                                ng-class="{'disabled' : !self.hasMatches}">
                                Matches
                                <span class="ntb__tabs__unread">{{ self.unreadMatches }}</span>
                            </a>
                        </li>
                        <li ng-class="{'ntb__tabs__selected' : self.selection == 2}">
                            <a ui-sref="overviewIncomingRequests({myUri: self.myNeedUri})"
                                ng-class="{'disabled' : !self.hasIncomingRequests}">
                                Requests
                                <span class="ntb__tabs__unread">{{ self.unreadIncomingRequests }}</span>
                            </a>
                        </li>
                        <li ng-class="{'ntb__tabs__selected' : self.selection == 3}">
                            <a ui-sref="overviewSentRequests({myUri: self.myNeedUri})"
                                ng-class="{'disabled' : !self.hasSentRequests}">
                                Sent Requests
                                <span class="ntb__tabs__unread">{{ self.unreadSentRequests }}</span>
                            </a>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>
    `;


    class Controller {
        constructor() {
            attach(this, serviceDependencies, arguments);

            window.otb = this;
            this.labels = labels;
            this.settingsOpen = false;

            const selectFromState = (state)=>{
                const unreadCounts = selectUnreadEventsByNeedAndType(state);
                const myNeedUri = decodeURIComponent(state.getIn(['router', 'currentParams', 'myUri']));
                const myNeed = state.getIn(['needs', 'ownNeeds', myNeedUri]);
                const connectionUrisOfNeed = myNeed.get('hasConnections');
                const connectionsOfNeed = connectionUrisOfNeed.map(connectionUri => state.getIn(['connections', connectionUri]));

                return {
                    myNeedUri: myNeedUri,
                    myNeed: myNeed,
                    hasIncomingRequests: connectionsOfNeed.filter(conn =>
                        conn.get('hasConnectionState') === won.WON.RequestReceived
                    ).size > 0,

                    hasSentRequests: connectionsOfNeed.filter(conn =>
                        conn.get('hasConnectionState') === won.WON.RequestSent
                    ).size > 0,

                    hasMatches: connectionsOfNeed.filter(conn =>
                        conn.get('hasConnectionState') === won.WON.Suggested
                    ).size > 0,

                    hasMessages: connectionsOfNeed.filter(conn =>
                        conn.get('hasConnectionState') === won.WON.Connected
                    ).size > 0,

                    unreadMessages: unreadCounts.getIn([myNeedUri, won.WON.Connected]), //TODO: NOT REALLY THE MESSAGE COUNT ONLY THE CONVERSATION COUNT
                    unreadIncomingRequests: unreadCounts.getIn([myNeedUri, won.WON.RequestReceived]),
                    unreadSentRequests: unreadCounts.getIn([myNeedUri, won.WON.RequestSent]),
                    unreadMatches: unreadCounts.getIn([myNeedUri, won.WON.Suggested]),
                    isActive: state.getIn(['needs','ownNeeds', myNeedUri, 'state']) === won.WON.Active
                };
            };

            const disconnect = this.$ngRedux.connect(selectFromState, actionCreators)(this);
            this.$scope.$on('$destroy', disconnect);
        }

        closePost() {
            console.log("CLOSING THE POST: " + this.myNeedUri);
            this.needs__close(this.myNeedUri);
        }

        reOpenPost() {
            console.log("RE-OPENING THE POST: " + this.myNeedUri);
            this.needs__reopen(this.myNeedUri);
        }
    }
    Controller.$inject = serviceDependencies;
    return {
        restrict: 'E',
        controller: Controller,
        controllerAs: 'self',
        bindToController: true, //scope-bindings -> ctrl
        template: template,
        scope: {selection: "=",
                item: "="}
    }
}

export default angular.module('won.owner.components.needTitleBar', [])
    .directive('wonOwnerTitleBar', genComponentConf)
    .name;
