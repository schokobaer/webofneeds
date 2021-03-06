import angular from "angular";
import Immutable from "immutable"; // also exports itself as (window).L
import L from "../../leaflet-bundleable.js";
import {
  attach,
  searchNominatim,
  reverseSearchNominatim,
  nominatim2draftLocation,
  delay,
  getIn,
} from "../../utils.js";
import { DomCache } from "../../cstm-ng-utils.js";

import { initLeaflet } from "../../won-utils.js";

const serviceDependencies = ["$scope", "$element", "$sce"];
function genComponentConf() {
  const prevLocationBlock = (
    displayBlock,
    selectLocationFnctName,
    prevLocation
  ) => `
  <!-- PREVIOUS LOCATION -->
  <li class="rp__searchresult" ng-if="${displayBlock}">
      <svg class="rp__searchresult__icon" style="--local-primary:var(--won-subtitle-gray);">
          <!-- TODO: create and use a more appropriate icon here -->
          <use xlink:href="#ico16_indicator_location" href="#ico16_indicator_location"></use>
      </svg>
      <a class="rp__searchresult__text" href=""
          ng-click="${selectLocationFnctName}(${prevLocation})"
          ng-bind-html="self.highlight(${prevLocation}.name, self.lastSearchedFor)">
      </a>
      (previous)
  </li>`;

  const searchResultsBlock = (searchResults, selectLocationFnctName) => `
  <!-- SEARCH RESULTS -->
  <li class="rp__searchresult" 
      ng-repeat="result in ${searchResults}">
      <svg class="rp__searchresult__icon" style="--local-primary:var(--won-subtitle-gray);">
          <use xlink:href="#ico16_indicator_location" href="#ico16_indicator_location"></use>
      </svg>
      <a class="rp__searchresult__text" href=""
          ng-click="${selectLocationFnctName}(result)"
          ng-bind-html="self.highlight(result.name, self.lastSearchedFor)">
      </a>
  </li>`;

  const template = `
        <!-- FROM LOCATION SEARCH BOX -->
        <div class="rp__searchbox">
            <input
                type="text"
                id="rp__from-searchbox__inner"
                class="rp__searchbox__inner"
                placeholder="Start Location"
                ng-class="{'rp__searchbox__inner--withreset' : self.fromShowResetButton}"/>
            <svg class="rp__searchbox__icon clickable" 
                 style="--local-primary:var(--won-primary-color);"
                 ng-if="self.fromShowResetButton"
                 ng-click="self.resetFromLocation()">
                    <use xlink:href="#ico36_close" href="#ico36_close"></use>
            </svg>
        </div>
        <!-- LIST OF SUGGESTED LOCATIONS -->
        <ul class="rp__searchresults" ng-class="{ 
            'rp__searchresults--filled': self.showFromResultDropDown(), 
            'rp__searchresults--empty': !self.showFromResultDropDown() 
        }">
            <!-- CURRENT GEOLOCATION -->
            <li class="rp__searchresult" 
                ng-if="self.showCurrentLocationResult()">
                <svg class="rp__searchresult__icon" style="--local-primary:var(--won-subtitle-gray);">
                    <use xlink:href="#ico16_indicator_location" href="#ico36_location_current"></use>
                </svg>
                <a class="rp__searchresult__text" href=""
                    ng-click="self.selectedFromLocation(self.currentLocation)"
                    ng-bind-html="self.highlight(self.currentLocation.name, self.lastSearchedFor)">
                </a>
            </li>
            
            ${prevLocationBlock(
              "self.showFromPrevLocationResult()",
              "self.selectedFromLocation",
              "self.fromPreviousLocation"
            )}
            ${searchResultsBlock(
              "self.fromSearchResults",
              "self.selectedFromLocation"
            )}
        </ul>

        <!-- TO LOCATION SEARCH BOX -->
        <div class="rp__searchbox">
            <input
                type="text"
                id="rp__to-searchbox__inner"
                class="rp__searchbox__inner"
                placeholder="Destination"
                ng-class="{'rp__searchbox__inner--withreset' : self.toShowResetButton}"/>
            <svg class="rp__searchbox__icon clickable" 
                 style="--local-primary:var(--won-primary-color);"
                 ng-if="self.toShowResetButton"
                 ng-click="self.resetToLocation()">
                    <use xlink:href="#ico36_close" href="#ico36_close"></use>
            </svg>
        </div>
        <!-- LIST OF SUGGESTED LOCATIONS -->
        <ul class="rp__searchresults" ng-class="{ 
            'rp__searchresults--filled': self.showToResultDropDown(), 
            'rp__searchresults--empty': !self.showToResultDropDown() 
        }">
   
            ${prevLocationBlock(
              "self.showToPrevLocationResult()",
              "self.selectedToLocation",
              "self.toPreviousLocation"
            )}
            ${searchResultsBlock(
              "self.toSearchResults",
              "self.selectedToLocation"
            )}
        </ul>

        <div class="rp__mapmount" id="rp__mapmount"></div>
            `;

  class Controller {
    constructor() {
      attach(this, serviceDependencies, arguments);
      this.domCache = new DomCache(this.$element);

      this.map = initLeaflet(this.mapMount());
      //this.map.on("click", e => onMapClick(e, this));

      // debug output
      window.rp4dbg = this;

      this.travelAction = {};

      this.fromAddedLocation = undefined;
      this.fromPreviousLocation = undefined;
      this.fromShowResetButton = false;

      this.toAddedLocation = undefined;
      this.toPreviousLocation = undefined;
      this.toShowResetButton = false;

      // only works if we have access to the current location
      this.determineCurrentLocation();

      // needs to happen after constructor finishes, otherwise
      // the component's callbacks won't be registered.
      delay(0).then(() => this.showInitialLocations());

      this.typingBuffer(e => this.doneTypingFrom(e), this.fromTextfield(), 300);
      this.typingBuffer(e => this.doneTypingTo(e), this.toTextfield(), 300);
    }

    typingBuffer(listenerCallback, domElement, doneTypingInterval) {
      let typingTimer;
      domElement.addEventListener("input", e => {
        if (typingTimer) {
          clearTimeout(typingTimer);
        }
        typingTimer = setTimeout(() => listenerCallback(e), doneTypingInterval);
      });
    }

    showInitialLocations() {
      if (this.initialTravelAction) {
        this.fromAddedLocation = this.initialTravelAction.fromLocation;
        this.toAddedLocation = this.initialTravelAction.toLocation;

        let markedLocations = [];

        if (this.initialTravelAction.fromLocation) {
          markedLocations.push(this.initialTravelAction.fromLocation);
          this.fromShowResetButton = true;
          this.fromTextfield().value = this.initialTravelAction.fromLocation.name;
        }

        if (this.initialTravelAction.toLocation) {
          markedLocations.push(this.initialTravelAction.toLocation);
          this.toShowResetButton = true;
          this.toTextfield().value = this.initialTravelAction.toLocation.name;
        }

        this.placeMarkers(markedLocations);
        this.map.fitBounds(
          this.getBoundCoords([this.fromAddedLocation, this.toAddedLocation]),
          {
            animate: true,
            maxZoom: 14,
          }
        );

        this.$scope.$apply();
      }
    }

    // TODO: implement
    checkValidity() {
      // validity check
      // set validity - invalid if exactly one location is undefined
    }

    selectedFromLocation(location) {
      // save new location value
      this.travelAction.fromLocation = location;
      this.travelAction.toLocation = this.toAddedLocation;
      this.onRouteUpdated({
        travelAction: this.travelAction,
      });
      this.fromAddedLocation = location;

      // represent new value to user
      this.checkValidity();
      this.resetSearchResults();
      this.fromTextfield().value = location.name;
      this.fromShowResetButton = true;

      let markers = [];
      markers.push(location);
      if (this.toAddedLocation) {
        markers.push(this.toAddedLocation);
      }
      this.placeMarkers(markers);
      this.markers[0].openPopup();
      this.map.fitBounds(
        this.getBoundCoords([location, this.toAddedLocation]),
        {
          animate: true,
          maxZoom: 14,
        }
      );
    }

    selectedToLocation(location) {
      // save new location value
      this.travelAction.fromLocation = this.fromAddedLocation;
      this.travelAction.toLocation = location;
      this.onRouteUpdated({
        travelAction: this.travelAction,
      });
      this.toAddedLocation = location;

      // represent new value to user
      this.checkValidity();
      this.resetSearchResults(); // picked one, can hide the rest if they were there
      this.toTextfield().value = location.name;
      this.toShowResetButton = true;

      let markers = [];
      markers.push(location);
      if (this.fromAddedLocation) {
        markers.push(this.fromAddedLocation);
      }
      this.placeMarkers(markers);
      this.markers[0].openPopup();
      this.map.fitBounds(
        this.getBoundCoords([location, this.fromAddedLocation]),
        {
          animate: true,
          maxZoom: 14,
        }
      );
    }

    doneTypingFrom() {
      const fromText = this.fromTextfield().value;

      this.resetToSearchResults(); // reset search results of other field

      if (this.fromAddedLocation !== undefined) {
        this.fromShowResetButton = false;
        this.$scope.$apply(() => {
          this.resetFromLocation();
        });
      }

      if (!fromText) {
        this.$scope.$apply(() => {
          this.resetFromSearchResults();
        });
      } else {
        // search for new results
        // TODO: sort results by distance/relevance/???
        // TODO: limit amount of shown results
        searchNominatim(fromText).then(searchResults => {
          const parsedResults = scrubSearchResults(searchResults, fromText);
          this.$scope.$apply(() => {
            this.fromSearchResults = parsedResults;
            this.lastSearchedFor = fromText;
          });
        });
      }
    }

    doneTypingTo() {
      const toText = this.toTextfield().value;

      this.resetFromSearchResults(); // reset search results of other field

      if (this.toAddedLocation !== undefined) {
        this.toShowResetButton = false;
        this.$scope.$apply(() => {
          this.resetToLocation();
        });
      }

      if (!toText) {
        this.$scope.$apply(() => {
          this.resetToSearchResults();
        });
      } else {
        // search for new results
        // TODO: sort results by distance/relevance/???
        // TODO: limit amount of shown results
        searchNominatim(toText).then(searchResults => {
          const parsedResults = scrubSearchResults(searchResults, toText);
          this.$scope.$apply(() => {
            this.toSearchResults = parsedResults;
            this.lastSearchedFor = toText;
          });
        });
      }
    }

    getBoundCoords(locations) {
      let coords = [];
      for (let location of locations) {
        if (location) {
          coords.push(new L.LatLng(location.lat, location.lng));
        }
      }
      return coords;
    }

    placeMarkers(locations) {
      if (this.markers) {
        //remove previously placed markers
        for (let m of this.markers) {
          this.map.removeLayer(m);
        }
      }

      this.markers = locations.map(location =>
        L.marker([location.lat, location.lng]).bindPopup(location.name)
      );

      for (let m of this.markers) {
        this.map.addLayer(m);
      }
    }

    resetLocations() {
      this.resetFromLocation();
      this.resetToLocation();
    }

    resetFromLocation() {
      this.fromPreviousLocation = this.fromAddedLocation;
      this.fromAddedLocation = undefined;

      let markers = [];
      if (this.toAddedLocation) {
        markers.push(this.toAddedLocation);
      }
      this.placeMarkers(markers);

      this.fromShowResetButton = false;
      this.fromTextfield().value = "";

      this.travelAction.fromLocation = undefined;
      this.travelAction.toLocation = this.toAddedLocation;
      this.onRouteUpdated({
        travelAction: this.travelAction,
      });

      this.checkValidity();
    }

    resetToLocation() {
      this.toPreviousLocation = this.toAddedLocation;
      this.toAddedLocation = undefined;

      let markers = [];
      if (this.fromAddedLocation) {
        markers.push(this.fromAddedLocation);
      }
      this.placeMarkers(markers);

      this.toShowResetButton = false;
      this.toTextfield().value = "";

      this.travelAction.fromLocation = this.fromAddedLocation;
      this.travelAction.toLocation = undefined;
      this.onRouteUpdated({
        travelAction: this.travelAction,
      });

      this.checkValidity();
    }

    resetSearchResults() {
      this.resetFromSearchResults();
      this.resetToSearchResults();
    }

    resetFromSearchResults() {
      this.fromSearchResults = undefined;
      this.lastSearchedFor = undefined;
    }
    resetToSearchResults() {
      this.toSearchResults = undefined;
      this.lastSearchedFor = undefined;
    }

    showCurrentLocationResult() {
      return !this.fromAddedLocation && !!this.currentLocation;
    }

    showFromPrevLocationResult() {
      return (
        !this.fromAddedLocation &&
        this.fromPreviousLocation &&
        getIn(this, ["fromPreviousLocation", "name"]) !==
          getIn(this, ["currentLocation", "name"])
      );
    }

    showFromResultDropdown() {
      return (
        (this.fromSearchResults && this.fromSearchResults.length > 0) ||
        this.showCurrentLocationResult() ||
        this.showFromPrevLocationResult()
      );
    }

    showToPrevLocationResult() {
      return (
        this.toAddedLocation === undefined &&
        this.toPreviousLocation !== undefined
      );
    }

    showToResultDropDown() {
      return (
        (this.toSearchResults && this.toSearchResults.length > 0) ||
        this.showToPrevLocationResult()
      );
    }

    determineCurrentLocation() {
      if ("geolocation" in navigator) {
        navigator.geolocation.getCurrentPosition(
          currentLocation => {
            const geoLat = currentLocation.coords.latitude;
            const geoLng = currentLocation.coords.longitude;
            const geoZoom = 13; // TODO: use `currentLocation.coords.accuracy` to control coarseness of query / zoom-level

            // center map around geolocation only if there's no initial location
            if (!this.initialTravelAction) {
              this.map.setZoom(geoZoom);
              this.map.panTo([geoLat, geoLng]);
            }

            reverseSearchNominatim(geoLat, geoLng, geoZoom).then(
              searchResult => {
                const location = nominatim2draftLocation(searchResult);
                this.$scope.$apply(() => {
                  this.currentLocation = location;
                });
              }
            );
          },
          err => {
            //error handler
            if (err.code === 2) {
              alert("Position is unavailable!"); //TODO toaster
            }
          },
          {
            //options
            enableHighAccuracy: true,
            timeout: 5000,
            maximumAge: 0,
          }
        );
      }
    }

    /**
     * Taken from <http://stackoverflow.com/questions/15519713/highlighting-a-filtered-result-in-angularjs>
     * @param text
     * @param search
     * @return {*}
     */
    highlight(text, search) {
      if (!text) {
        text = "";
      }
      if (!search) {
        return this.$sce.trustAsHtml(text);
      }
      return this.$sce.trustAsHtml(
        text.replace(
          new RegExp(search, "gi"),
          '<span class="highlightedText">$&</span>'
        )
      );
    }

    fromTextfieldNg() {
      return this.domCache.ng("#rp__from-searchbox__inner");
    }

    fromTextfield() {
      return this.domCache.dom("#rp__from-searchbox__inner");
    }

    toTextfieldNg() {
      return this.domCache.ng("#rp__to-searchbox__inner");
    }

    toTextfield() {
      return this.domCache.dom("#rp__to-searchbox__inner");
    }

    mapMountNg() {
      return this.domCache.ng(".rp__mapmount");
    }

    mapMount() {
      return this.domCache.dom(".rp__mapmount");
    }
  }
  Controller.$inject = serviceDependencies;

  return {
    restrict: "E",
    controller: Controller,
    controllerAs: "self",
    bindToController: true, //scope-bindings -> ctrl
    scope: {
      onRouteUpdated: "&",
      initialTravelAction: "=",
    },
    template: template,
  };
}

function scrubSearchResults(searchResults) {
  return (
    Immutable.fromJS(searchResults.map(nominatim2draftLocation))
      /*
                   * filter "duplicate" results (e.g. "Wien"
                   *  -> 1x waterway, 1x boundary, 1x place)
                   */
      .groupBy(r => r.get("name"))
      .map(sameNamedResults => sameNamedResults.first())
      .toList()
      .toJS()
  );
}

// TODO: decide on appropriate behaviour and implement it.
// function onMapClick(e, ctrl) {
//   //`this` is the mapcontainer here as leaflet
//   // apparently binds itself to the function.
//   // This code was moved out of the controller
//   // here to avoid confusion resulting from
//   // this binding.
//   reverseSearchNominatim(
//     e.latlng.lat,
//     e.latlng.lng,
//     ctrl.map.getZoom() // - 1
//   ).then(searchResult => {
//     const location = nominatim2draftLocation(searchResult);

//     //use coords of original click though (to allow more detailed control)
//     location.lat = e.latlng.lat;
//     location.lng = e.latlng.lng;
//     ctrl.$scope.$apply(() => {
//       ctrl.selectedLocation(location);
//     });
//   });
// }

export default angular
  .module("won.owner.components.routePicker", [])
  .directive("wonRoutePicker", genComponentConf).name;

window.searchNominatim4dbg = searchNominatim;
window.reverseSearchNominatim4dbg = reverseSearchNominatim;
window.nominatim2wonLocation4dbg = nominatim2draftLocation;
