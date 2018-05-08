/**
 * Created by ksinger on 20.08.2015.
 */
;
import angular from 'angular';
import { attach } from '../utils.js';
import { actionCreators }  from '../actions/actions.js';
import {
    connect2Redux,
} from '../won-utils.js';

import * as srefUtils from '../sref-utils.js';

function genComponentConf() {
    let template = `
    <span class="dd__userlabel show-in-responsive" ng-if="self.loggedIn" title="{{ self.email }}">{{ self.email }}</span>
    <hr class="show-in-responsive"/>
    <a
        href="{{ self.absHRef(self.$state, 'about') }}"
        class="won-button--outlined thin red"
        ng-click="self.hideMainMenuDisplay()">
            About
    </a>
    <a class="won-button--outlined thin red"
        ng-click="self.toggleRdfDisplay()">
        <svg class="won-button-icon" style="--local-primary:var(--won-primary-color);">
            <use xlink:href="#ico36_rdf_logo" href="#ico36_rdf_logo"></use>
        </svg>
        <span>{{self.shouldShowRdf? "Hide raw data" : "Show raw RDF data"}}</span>
    </a>
    <hr/>
    <button
        class="won-button--filled lighterblue"
        style="width:100%"
        ng-click="::self.logout()">
            Sign out
    </button>
    `;

    const serviceDependencies = ['$ngRedux', '$scope', '$state',/*'$routeParams' /*injections as strings here*/];

    class Controller {
        constructor(/* arguments <- serviceDependencies */){
            attach(this, serviceDependencies, arguments);
            Object.assign(this, srefUtils);

            window.lopu4dbg = this;

            this.email = "";
            this.password = "";

            const logout = (state) => ({
                loggedIn: state.getIn(['user','loggedIn']),
                shouldShowRdf: state.get('showRdf'),
                email: state.getIn(['user','email'])
            });

            connect2Redux(logout, actionCreators, [], this);
        }
    }
    Controller.$inject = serviceDependencies;

    return {
        restrict: 'E',
        controller: Controller,
        controllerAs: 'self',
        bindToController: true, //scope-bindings -> ctrl
        scope: {},
        template: template
    }
}

export default angular.module('won.owner.components.logout', [])
    .directive('wonLoggedInMenu', genComponentConf)
    .name;

