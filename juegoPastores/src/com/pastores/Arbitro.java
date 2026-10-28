package com.pastores;

import java.util.Arrays;
import java.util.Scanner;
import java.io.*;

public class Arbitro {

	private static final int MINIMO = 1;
	private static final int NUM_MODOS = 3;
	private static final String RUTAS_TXT="resources/rutas.txt";
	private static final String GAMEOVER = "GAMEOVER";
	
	private static final String PIEDRA = "O";  //Símbolo de piedra
	
	public static void main(String[] args) {
		
		String[] rutas = leerRutas(RUTAS_TXT);

		if(rutas[0] != null && rutas[1] != null)
		{
			try(Scanner sc = new Scanner(System.in))
			{
				iniciarJuego(sc, rutas);
			}
		}
		else
		{
			System.out.println("No se pudo leer: " + RUTAS_TXT);
		}
				
	}
	
	private static Process iniciarProceso(ProcessBuilder pb)
	{
		Process proceso;
		try {
			proceso = pb.start();
		} catch (IOException e) {
			System.out.println("Error al lanzar la IA");
			proceso = null;
		}
		return proceso;
	}
	
	private static void iniciarJuego(Scanner sc, String[] rutas)
	{
		int modo = leerModo(sc);
		int numFilas = leerEntero(sc, "Número de filas:");
		int piedras = leerEntero(sc, "Piedras por fila:");

		switch(modo)
		{
			case 1 -> unaPartida(numFilas, piedras, rutas, true);
			case 2 -> unaPartida(numFilas, piedras, rutas, false);
			case 3 -> competicion(sc, numFilas, piedras, rutas);
		}
	}
	
	private static void escribirLinea(BufferedWriter bw, String linea)
	{
		try {
			bw.write(linea);
			bw.newLine();
			bw.flush();
		} catch (IOException e) {
			System.out.println("Error al intentar escribir respuesta");
			e.printStackTrace();
		}
	}
	
	//Lee cualquier entero, dimensiones y mínimos
	private static int leerEntero(Scanner sc, String mensaje)  
	{
		int numero = MINIMO - 1;
		while(numero < MINIMO)
		{
			System.out.println(mensaje);
			String respuesta = sc.nextLine();
			try {
				numero = Integer.parseInt(respuesta.trim());
				if(numero < MINIMO)
				{
					System.out.println("El número debe ser al menos " + MINIMO);
				}
			}catch(NumberFormatException nfe)
			{
				System.out.println("No es un número válido");
			}
		}
		return numero;
	}
	
	//Devuelve el string[] con las dos rutas
	private static String[] leerRutas(String ruta)
	{
		String[]rutas = new String[2];
		
		try(BufferedReader br = new BufferedReader(new FileReader(ruta)))
		{	
			for(int i = 0; i < rutas.length;++i)
			{
				rutas[i] = br.readLine();
			}
			
		}catch(IOException e)
		{
			System.out.println("Error al intentar leer: " + ruta);
			e.printStackTrace();
		}
		
		return rutas;
	}

	private static Process lanzarIA(String linea)
	{
		String[] partesRuta = linea.split(",");

		ProcessBuilder pb = new ProcessBuilder("java", "-cp", partesRuta[0].trim(), partesRuta[1].trim());
		pb.redirectError(ProcessBuilder.Redirect.INHERIT); //QUITAR

		return iniciarProceso(pb);
	}
	
	
	
	private static String tableroATexto(int[] filas)
	{
		StringBuilder texto = new StringBuilder();
		
		for(int i = 0;i<filas.length;++i)
		{
			if(i>0)
			{
				texto.append(",");
			}
			
			texto.append(filas[i]);
		}
		return texto.toString();
	}
	
	//comprueba el input que le envian las ias
	private static boolean jugadaValida(int[] filas, int fila, int cantidad)
	{
		return fila >= 0 && fila < filas.length && cantidad >= 1 && cantidad <= filas[fila];
	}
	
	//funcion aux para comprobar que la jugada de la ia es correcta y devuelve el valor en int[]
	private static int[] leerJugada(BufferedReader lector)
	{
		int[]jugada = {-1,-1};
		try {
			String[] partes = lector.readLine().trim().split(",");
			
			jugada[0] = Integer.parseInt(partes[0].trim());
			jugada[1] = Integer.parseInt(partes[1].trim());
			
		} catch (IOException | RuntimeException e) {
			System.out.println("Respuesta de la IA no válida");
		}

		return jugada;
	}
	
	private static int contarPiedras(int[] filas)
	{
		int total = 0;
		for(int f : filas)
		{
			total += f;
		}
		return total;
	}
	
	//invocar después de cada jugada para comprobar si sigue la partida
	//jugadorTurno es el jugador que hace el movimiento
	private static int comprobarGanador(int[] filas, int jugadorTurno)
	{
		int piedras = contarPiedras(filas);
		int ganador = -1;

		if(piedras == 1)
		{
			ganador = jugadorTurno;
		}
		else if(piedras == 0)
		{
			ganador = 1 - jugadorTurno;
		}

		return ganador;
	}
	
	private static void mostrarTablero(int[]filas)
	{
		for(int piedrasFila : filas)
		{
			System.out.println(PIEDRA.repeat(piedrasFila));
		}
	}
	
	//para describir la jugada al usuario
	private static void describirJugada(int jugadorTurno, int[] jugada)
	{
		System.out.println("IA " + (jugadorTurno + 1) + " quita " + jugada[1] + " piedras de la fila " + (jugada[0] + 1));
	}
	
	private static int leerModo(Scanner sc)
	{
		int modo = 0;
		while(modo < MINIMO || modo > NUM_MODOS)
		{
			modo = leerEntero(sc,"1. Normal  2. Partida rápida  3. Competición");
			if(modo > NUM_MODOS)
			{
				System.out.println("Elige entre 1 y " + NUM_MODOS);
			}
			
		}
		return modo;
	}
	
	private static Process[] arrancarIAs(String[] rutas)
	{
		Process[] procesos = new Process[rutas.length];

		for(int i = 0; i < rutas.length; i++)
		{
			procesos[i] = lanzarIA(rutas[i]);
		}
		return procesos;
	}
	
	private static void cerrarIAs(Process[] procesos)
	{
		for(Process proceso : procesos)
		{
			if(proceso != null)
			{
				escribirLinea(proceso.outputWriter(), GAMEOVER);
			}
		}
	}
	
	private static void unaPartida(int numFilas, int piedras, String[] rutas, boolean mostrar)
	{
		int[] resultado = jugarPartida(numFilas, piedras, mostrar, 0, rutas);

		if(resultado[0] == -1)
		{
			System.out.println("Las IAs no pudieron arrancar");
		}
		else
		{
			System.out.println("Gana la IA " + (resultado[0] + 1) + " en " + resultado[1] + " jugadas");
		}
	}
	
	/**
	 * Modo competición: juega varias partidas alternando quién empieza
	 * y muestra las estadísticas finales.
	 */
	private static void competicion(Scanner sc, int numFilas, int piedras, String[] rutas)
	{
		int numPartidas = leerEntero(sc, "Número de partidas:");
		int[] victorias = new int[2];
		int[] jugadasEnVictorias = new int[2];

		for(int partida = 0; partida < numPartidas; partida++)
		{
			int[] resultado = jugarPartida(numFilas, piedras, false, partida % 2, rutas);

			if(resultado[0] != -1)
			{
				victorias[resultado[0]]++;
				jugadasEnVictorias[resultado[0]] += resultado[1];
			}
		}

		mostrarEstadisticas(victorias, jugadasEnVictorias, numPartidas);
	}

	/**
	 * Muestra por cada IA: victorias, porcentaje y media de jugadas por victoria.
	 */
	private static void mostrarEstadisticas(int[] victorias, int[] jugadasEnVictorias, int numPartidas)
	{
		for(int i = 0; i < victorias.length; i++)
		{
			double porcentaje = victorias[i] * 100.0 / numPartidas;
			double media = 0;

			if(victorias[i] > 0)
			{
				media = (double) jugadasEnVictorias[i] / victorias[i];
			}

			System.out.printf("IA %d: %d victorias, %.1f%%, media %.1f jugadas por victoria%n",
					i + 1, victorias[i], porcentaje, media);
		}
	}
	
	private static int[] jugarPartida(int numFilas, int piedrasPorFila, boolean mostrar, int primero, String[] rutas)
	{
		int[] filas = new int[numFilas];
		Arrays.fill(filas, piedrasPorFila);
		Process[] procesos = arrancarIAs(rutas);
		int jugadorTurno = primero;
		int jugadas = 0;
		int ganador = -1;

		if(procesos[0] != null && procesos[1] != null)
		{
			if(mostrar)
			{
				mostrarTablero(filas);
			}

			while(ganador == -1)
			{
				escribirLinea(procesos[jugadorTurno].outputWriter(), tableroATexto(filas));
				int[] jugada = leerJugada(procesos[jugadorTurno].inputReader());

				if(jugadaValida(filas, jugada[0], jugada[1]))
				{
					filas[jugada[0]] -= jugada[1];
					jugadas++;

					if(mostrar)
					{
						describirJugada(jugadorTurno, jugada);
						mostrarTablero(filas);
					}

					ganador = comprobarGanador(filas, jugadorTurno);
					jugadorTurno = 1 - jugadorTurno;
				}
				else
				{
					if(mostrar)
					{
						System.out.println("Jugada ilegal de la IA " + (jugadorTurno + 1));
					}
					ganador = 1 - jugadorTurno;
				}
			}
		}
		cerrarIAs(procesos);

		return new int[]{ganador, jugadas};

	}
	
}
