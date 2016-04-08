/**
 * Created by ksinger on 22.03.2016.
 */
;

import won from '../won-es6';
import angular from 'angular';
import squareImageModule from './square-image';
import { labels } from '../won-label-utils';
import { attach } from '../utils.js';
import { actionCreators }  from '../actions/actions';
import { selectAllByConnections } from '../selectors';

const serviceDependencies = ['$ngRedux', '$scope'];
function genComponentConf() {
    let template = `
        <div class="connectionSelectionItemLine"
                ng-repeat="(key,connectionUri) in self.relevantConnectionUris">
            <div class="conn">
                 <div
                 class="conn__item clickable"
                 ng-class="self.openUri === connectionUri? 'selected' : ''"
                 ng-click="self.setOpen(connectionUri)">
                     <!--TODO request.titleImgSrc isn't defined -->
                    <won-square-image
                        src="request.titleImgSrc"
                        title="self.allByConnections.getIn([connectionUri, 'remoteNeed', 'title'])">
                    </won-square-image>
                    <div class="conn__item__description">
                        <div class="conn__item__description__topline">
                            <div class="conn__item__description__topline__title">
                                {{ self.allByConnections.getIn([connectionUri, 'remoteNeed', 'title']) }}
                            </div>
                            <div class="conn__item__description__topline__date">
                                {{ self.allByConnections.getIn([connectionUri, 'connection', 'timestamp']) }}
                            </div>
                            <img
                                class="conn__item__description__topline__icon"
                                src="generated/icon-sprite.svg#ico_settings">
                        </div>
                        <div class="conn__item__description__subtitle">
                            <span class="conn__item__description__subtitle__group" ng-show="request.group">
                                <img
                                    src="generated/icon-sprite.svg#ico36_group"
                                    class="mil__item__description__subtitle__group__icon">
                                {{ self.allByConnections.getIn([connectionUri, 'group']) }}
                                <span class="mil__item__description__subtitle__group__dash"> &ndash; </span>
                            </span>
                            <span class="conn__item__description__subtitle__type">
                                {{
                                   self.labels.type[
                                        self.allByConnections.getIn([connectionUri, 'remoteNeed', 'basicNeedType'])
                                   ]
                                }}
                            </span>
                        </div>
                        <!--
                        <div class="conn__item__description__message">
                            <span
                                class="conn__item__description__message__indicator"
                                ng-click="self.setOpen(connectionUri)"
                                ng-show="!self.read(connectionUri))"/>
                                <!-- TODO self.read isn't defined
                            {{ self.allByConnections.getIn([connectionUri, 'lastEvent', 'msg']) }}
                        </div>
                        -->
                    </div>
                </div>
            </div>
        </div>
    `;

    class Controller {
        constructor() {
            window.connSel4db = this;
            attach(this, serviceDependencies, arguments);
            this.labels = labels;
            this.openUri = '';

            const selectFromState = (state)=>{
                const myNeedUri = decodeURIComponent(state.getIn(
                    ['router', 'currentParams', 'myUri']));

                const myNeed = state.getIn(['needs','ownNeeds', myNeedUri]);
                const allByConnections = selectAllByConnections(state);

                if(this.connectionType) {
                    const connectionUrisOfNeed = state.getIn(
                        ['needs', 'ownNeeds', myNeedUri, 'hasConnections']);

                    const relevantConnections = connectionUrisOfNeed
                        .map(connectionUri =>
                            state.getIn(['connections', connectionUri]))
                        .filter(conn =>
                            conn.get('hasConnectionState') === this.connectionType
                        );

                    const relevantConnectionUris = relevantConnections.map(c => c.get('uri'));

                    return {
                        post: myNeed.toJS(),  //TODO plz don't do `.toJS()`. every time an ng-binding somewhere cries.
                        allByConnections,
                        relevantConnectionUris: relevantConnectionUris.toJS(), //TODO plz don't do `.toJS()`. every time an ng-binding somewhere cries.
                    }
                } else {
                    return {
                        post: myNeed.toJS(),  //TODO plz don't do `.toJS()`. every time an ng-binding somewhere cries.
                        allByConnections,
                        relevantConnectionUris: this.connectionUris? this.connectionUris : [],
                    }
                };
            }

            const disconnect = this.$ngRedux.connect(selectFromState, actionCreators)(this);
            this.$scope.$on('$destroy', disconnect);
        }
        setOpen(connectionUri) {
            this.openUri = connectionUri;
            this.selectedConnection({connectionUri}); //trigger callback with scope-object
        }
        /*
        getOpen() {
            return this.allByConnections.get(this.openUri);
        }

        openMessage(item) {
            //this.events__read(item)
            this.openConversation = item;
            this.selectedConnectionUri = item.connection.uri;
            this.selectedConnection(item.connection.uri);
        }
        */
    }
    Controller.$inject = serviceDependencies;
    return {
        restrict: 'E',
        controller: Controller,
        controllerAs: 'self',
        bindToController: true, //scope-bindings -> ctrl
        scope: {
            connectionType: "=",
            /**
             * @deprecated caused some issues for me. Actually in all cases
             * we only need the connection-type and can get the rest from the
             * routing parameters.
             */
            connectionUris: "=",
            /*
             * Usage:
             *  selected-connection="myCallback(connectionUri)"
             */
            selectedConnection: "&"
        },
        template: template
    }

}

export default angular.module('won.owner.components.connectionSelection', [])
    .directive('wonConnectionSelection', genComponentConf)
    .name;
