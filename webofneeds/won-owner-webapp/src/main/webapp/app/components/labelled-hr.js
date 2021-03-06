import angular from "angular";

function genComponentConf() {
  let template = `<div class="wlh__label">
    					<span class="wlh__label__text" ng-show="!self.arrow">{{ self.label }}</span>
    					<svg class="wlh__label__carret clickable" ng-show="self.arrow == 'down'">
	                    	<use xlink:href="#ico16_arrow_down" href="#ico16_arrow_down"></use>
	                    </svg>
    					<svg class="wlh__label__carret clickable" ng-show="self.arrow == 'up'">
	                    	<use xlink:href="#ico16_arrow_up" href="#ico16_arrow_up"></use>
	                    </svg>
	                </div>`;

  class Controller {
    constructor() {}
  }

  return {
    restrict: "E",
    controller: Controller,
    controllerAs: "self",
    bindToController: true, //scope-bindings -> ctrl
    scope: {
      label: "=",
      arrow: "=",
    },
    template: template,
  };
}

export default angular
  .module("won.owner.components.labelledHr", [])
  .directive("wonLabelledHr", genComponentConf).name;
