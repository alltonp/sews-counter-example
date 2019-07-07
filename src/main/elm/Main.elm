module Main exposing (Model, Msg(..), init, main, sendToServer, subscriptions, update, view)

import Codec exposing (..)
import Html exposing (..)
import Html.Events exposing (onClick)
import Json.Decode
import Json.Encode
import WebSocket


type alias Model =
    { count : Int
    }


type Msg
    = FromServer String
    | OnInit
    | ClickIncrement
    | ClickDecrement



--this is still a bit wrong, model should come from server, introduce Maybe or RemoteData


init : ( Model, Cmd Msg )
init =
    update OnInit (Model 0)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        FromServer str ->
            let
                model_ =
                    case Json.Decode.decodeString decodeFromServer str of
                        Ok msg ->
                            case msg of
                                FromServerModelUpdated m ->
                                    { model | count = m.model.count }

                        Err x ->
                            --                            { model | result = Err x }
                            model
            in
            ( model_, Cmd.none )

        OnInit ->
            ( model, sendToServer (ToServerInit Init) )

        ClickIncrement ->
            ( model, sendToServer (ToServerIncrement Increment) )

        ClickDecrement ->
            ( model, sendToServer (ToServerDecrement Decrement) )


view : Model -> Html Msg
view model =
    div []
        [ button [ onClick ClickDecrement ] [ text "-" ]
        , div [] [ text (toString model.count) ]
        , button [ onClick ClickIncrement ] [ text "+" ]
        ]


sendToServer : ToServer -> Cmd msg
sendToServer serverMsg =
    WebSocket.send "ws://localhost:9000/ws/test" (Json.Encode.encode 0 (encodeToServer serverMsg))



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    WebSocket.listen "ws://localhost:9000/ws/test" FromServer


main =
    Html.program
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }
