Pastas e ficheiros obrigatórios:
	.Diretoria "ClientDir"
	.Diretoria "ServerDir"
		.Ficheiro "clients.txt"
	.Diretoria "ServerSecondaryDir"
		.Ficheiro "clients.txt"

Organização:
	.A pasta "ClientDir" tem de estar na mesma diretoria que o ficheiro "terminal.jar"
	.As pastas "ServerDir" e "ServerSecondaryDir" têm de estar na mesma diretoria que o ficheiro "ucDrive.jar"
	.Dentro das pastas "ServerDir" e "ServerSecondaryDir" tem de existir um ficheiro "clients.txt" com a seguinte estrutura: 
		username1|password1|
		username2|password2|
		(...)

A ter em conta:
	.O projeto foi compilado usando a seguinte versão do JAVA: java 17.0.1

Iniciar o programa:
	.Abrir consola, ir para diretoria dos ficheiros .jar e iniciar cada um com:
		Servidor Principal:java -jar ucDrive.jar
		Servidor Secundário:java -jar ucDrive.jar
		Cliente:java -jar terminal.jar