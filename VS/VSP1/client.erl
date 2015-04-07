-module(client).
-import(werkzeug, [get_config_value/2, timeMilliSecond/0, logging/2]).
-import(messageSendTimer, [changeSendInterval/1]).
-export([loop/3]).

-define(HOSTNAME, inet:gethostname()).
-define(GROUP, 3).
-define(TEAM, 01).
-define(LOGFILE, lists:flatten(io_lib:format("client_~p~p.log", [?TEAM, node()]))).

%Die Nummern in den Kommentaren beziehen sich auf:
%Das Diagramm "Client-Komponente"

%Einstiegstpunkt des Clients, welcher im lifetimeTimer aufgerufen wird
loop(Servername, Servernode, Sendinterval) ->
	%Rolle des Redakteur-Clients
	SendintervalNew = loopEditor(Servername, Servernode, Sendinterval),
	logging(?LOGFILE, lists:flatten(io_lib:format("dropmessages..Done~n", []))),
	%Rolle des Leser-Clients
	loopReader(Servername, Servernode, []),
	logging(?LOGFILE, lists:flatten(io_lib:format("getmessages..Done~n", []))),
	loop(Servername, Servernode, SendintervalNew).

loopEditor(Servername, Servernode, Sendinterval) ->
	loopEditor(Servername, Servernode, Sendinterval, 0).


loopEditor(Servername, Servernode, Sendinterval, Counter) ->	
	%1. neue Nachrichten Nummer besorgen
	Number = getMSGNum(Servername, Servernode),
	%3.1. neue Nachricht generieren 
	{ok, Client} = ?HOSTNAME, 
	Msg = lists:flatten(io_lib:format("~p-~p-client@~s-~p-~pte_Nachricht. C Out: ", [?GROUP, ?TEAM, Client, self(), Number])),
	%4. einen bestimmten Zeitabstand warten
	timer:sleep(Sendinterval * 1000),
	%3.2. der Timestamp darf erst nach dem abwarten des Sendeintervalls erzeugt
	%und der Nachricht angehangen werden
	Timestamp = timeMilliSecond(),
	MsgNew = Msg ++ Timestamp,
	%5. generierte Nachricht senden
	{Servername, Servernode} ! {dropmessage, [Number, MsgNew, Timestamp]},
	logging(?LOGFILE, lists:flatten(io_lib:format("~p gesendet~n", [MsgNew]))),
	%6. wurden alle 5 Nachrichten gesendet, wird die Rolle gewechselt 
	case Counter < 4 of
		true ->
			loopEditor(Servername, Servernode, Sendinterval, Counter + 1);
		false ->
			changeRole(Servername, Servernode, Sendinterval)
	end.

%Zeitabstand neu berechnen, sowie neue Nachrichtennummer anfordern, Nachricht generieren und diese loggen aber nicht verschickt
changeRole(Servername, Servernode, Sendinterval) ->
	%7. Zeitabstand aendern
	SendintervalNew = changeSendInterval(Sendinterval),
	%8. neue Nachrichten Nummer besorgen
	ForgetNumber = getMSGNum(Servername, Servernode),
	logging(?LOGFILE, lists:flatten(io_lib:format("~pte_Nachricht um " ++ timeMilliSecond() ++ " vergessen zu senden~n", [ForgetNumber]))),
	logging(?LOGFILE, lists:flatten(io_lib:format("neues Sendeintervall: ~p Sekunden (~p)~n", [SendintervalNew, Sendinterval]))),
	SendintervalNew.

loopReader(Servername, Servernode, NumberList) ->
	%10. Nachrichten abfragen
	{Servername, Servernode} ! {self(), getmessages},
	receive
		%11. auf Nachricht warten
		%13 Gibt es weitere Nachrichten? -> durch Terminated-Falg realisiert
		{reply, [NNr, Msg, _TSclientout, _TShbqin, _TSdlqin, _TSdlqout], false} ->
			%12. eigene Nachricht markieren
			logging(?LOGFILE, lists:flatten(io_lib:format("~p received new message; C In: " ++  timeMilliSecond() ++ "~n", [Msg]))),
			loopReader(Servername, Servernode, NumberList ++ [NNr]);
		{reply, [_NNr, Msg, _TSclientout, _TShbqin, _TSdlqin, _TSdlqout], true} ->
			logging(?LOGFILE, lists:flatten(io_lib:format("~p received last message; C In: " ++  timeMilliSecond() ++ "~n", [Msg])));
		{interrupt, timeout} ->
			logging(?LOGFILE, "reader-client interruted: timeout " ++ timeMilliSecond() ++ "~n"),
			exit(self(), "reader-client interrupted: timeout~n");
		Any ->
			io:format("reader-client received anything else: ~p~n", [Any])
	end.

%besorgt sich vom Server die naechste Nachrichtennummer
%wurde etwas unbekanntes vom Server empfangen wird -1 als Fehlermeldung zurueck gegeben
getMSGNum(Servername, Servernode) ->
	{Servername, Servernode} ! {self(), getmsgid},
	%2/9 auf neue Nachrichten Nummer warten
	receive
		{nid, Number} ->
			Number;
		{interrupt, timeout} ->
			logging(?LOGFILE, "reader-client interruted: timeout " ++ timeMilliSecond()),
			exit(self(), "reader-client interrupted: timeout~n"),
			Number = -1;
		Any ->
			io:format("reader-client received anything else: ~p~n", [Any]),
			Number = -1			
	end,
	Number.
