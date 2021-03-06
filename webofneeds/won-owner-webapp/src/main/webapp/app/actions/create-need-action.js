/**
 * Created by ksinger on 04.08.2017.
 */

import { buildCreateMessage } from "../won-message-utils.js";

import { actionCreators, actionTypes } from "./actions.js";

import { ensureLoggedIn } from "./account-actions.js";

import {
  getIn,
  reverseSearchNominatim,
  nominatim2draftLocation,
} from "../utils.js";

export function needCreate(draft, nodeUri) {
  return (dispatch, getState) => {
    const state = getState();

    if (!nodeUri) {
      nodeUri = getIn(state, ["config", "defaultNodeUri"]);
    }

    let prevParams = getIn(state, ["router", "prevParams"]);

    if (!state.getIn(["user", "loggedIn"]) && prevParams.privateId) {
      /*
             * `ensureLoggedIn` will generate a new privateId. should
             * there be a previous privateId, we don't want to change
             * back to that later.
             */
      prevParams = Object.assign({}, prevParams);
      delete prevParams.privateId;
    }

    return ensureLoggedIn(dispatch, getState)
      .then(() => {
        return dispatch(actionCreators.router__stateGoDefault());
      })
      .then(async () => {
        const { message, eventUri, needUri } = await buildCreateMessage(
          draft,
          nodeUri
        );
        dispatch({
          type: actionTypes.needs.create,
          payload: { eventUri, message, needUri, need: draft },
        });

        dispatch(
          actionCreators.router__stateGoAbs("connections", {
            postUri: undefined,
            showCreateView: undefined,
            connectionUri: undefined,
          })
        );
      });
  };
}

export function createWhatsNew() {
  return (dispatch, getState) => {
    console.log("Create Whats New");
    const state = getState();
    const nodeUri = getIn(state, ["config", "defaultNodeUri"]);
    const defaultContext = getIn(state, ["config", "theme", "defaultContext"]);
    const openWhatsXNeeds = getIn(state, ["needs"]).filter(
      need =>
        need.get("state") === "won:Active" &&
        (need.get("isWhatsAround") || need.get("isWhatsNew"))
    );

    if (openWhatsXNeeds.size == 0) {
      const whatsNew = {
        title: "What's New?",
        type: "http://purl.org/webofneeds/model#DoTogether",
        description:
          "Automatically created post to see what's happening recently",
        tags: undefined,
        thumbnail: undefined,
        whatsNew: true,
        matchingContext: defaultContext,
      };

      //TODO: Point to same DataSet instead of double it
      const whatsNewObject = {
        is: whatsNew,
        seeks: whatsNew,
      };

      dispatch(actionCreators.needs__create(whatsNewObject, nodeUri));
    } else {
      console.log("WhatsXNeed already exists, we will not create a new one");
    }
  };
}

export function createWhatsAround() {
  return (dispatch, getState) => {
    console.log("Create Whats Around");
    const state = getState();
    const nodeUri = getIn(state, ["config", "defaultNodeUri"]);
    const defaultContext = getIn(state, ["config", "theme", "defaultContext"]);
    const openWhatsAroundNeeds = getIn(state, ["needs"]).filter(
      need => need.get("state") === "won:Active" && need.get("isWhatsAround")
    );

    if ("geolocation" in navigator) {
      navigator.geolocation.getCurrentPosition(
        currentLocation => {
          const lat = currentLocation.coords.latitude;
          const lng = currentLocation.coords.longitude;
          const zoom = 13; // TODO use `currentLocation.coords.accuracy` to control coarseness of query / zoom-level

          if (openWhatsAroundNeeds.size == 0) {
            reverseSearchNominatim(lat, lng, zoom).then(searchResult => {
              const location = nominatim2draftLocation(searchResult);
              let whatsAround = {
                title: "What's Around?",
                type: "http://purl.org/webofneeds/model#DoTogether",
                description:
                  "Automatically created post to see what's happening in your area",
                tags: undefined,
                location: location,
                thumbnail: undefined,
                whatsAround: true,
                matchingContext: defaultContext,
              };

              getIn(state, ["needs"])
                .filter(
                  need =>
                    need.get("state") === "won:Active" && need.get("isWhatsNew")
                )
                .map(need => {
                  dispatch(actionCreators.needs__close(need.get("uri")));
                });
              const whatsAroundObject = {
                is: whatsAround,
                seeks: whatsAround,
              };

              dispatch(
                actionCreators.needs__create(whatsAroundObject, nodeUri)
              );
            });
          } else {
            console.log(
              "WhatsAroundNeed already exists, we will not create a new one"
            );
          }
        },
        error => {
          //error handler
          console.error(
            "Could not retrieve geolocation due to error: ",
            error.code,
            "fullerror:",
            error
          );
        },
        {
          //options
          enableHighAccuracy: true,
          timeout: 5000,
          maximumAge: 0,
        }
      );
    }
  };
}
