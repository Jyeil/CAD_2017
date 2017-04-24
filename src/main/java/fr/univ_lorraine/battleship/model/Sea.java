package fr.univ_lorraine.battleship.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

/**
 * Classe repr�sentant la grille d'un joueur.
 */
public class Sea extends Observable implements Serializable {

	/**
	 * Id pour la serialization.
	 * @serial
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Largeur de la grille.
	 */
	private static final int GRID_WIDTH = 10;
	
	/**
	 * Hauteur de la grille.
	 */
	private static final int GRID_HEIGHT = 10;
	
	/**
	 * Ensemble des tailles des bateaux pr�sents sur la grille.
	 */
	private static final int[] SHIPS_SIZES = { 5, 4, 3, 3, 2};
	
	/**
	 * Enum�ration des �tats d'une case de la grille.
	 */
	public enum SeaTileState { NORMAL, SHOT, TOUCHED }
	
	/**
	 * Etats des cases de la grille.
	 * @serial
	 */
	private SeaTileState[][] grid;
	
	/**
	 * Les bateaux qui ne sont pas encore plac� sur la grille.
	 * @serial
	 */
	private List<Ship> shipsToPlace;
	
	/**
	 * Le bateau en cours de placement.
	 * @serial
	 */
	private Ship shipOnPlacing;
	
	/**
	 * Les bateaux plac�s sur la grille.
	 * @serial
	 */
	private List<Ship> ships;
	
	/**
	 * Cr�e une grille � partir de l'�poque associ�e.
	 * @param epoch L'�poque.
	 */
	public Sea(Epoch epoch) {
		// Initialisation de la grille
		this.grid = new SeaTileState[GRID_WIDTH][GRID_HEIGHT];
		for (int i = 0 ; i < grid.length ; i++) {
			Arrays.fill(this.grid[i], SeaTileState.NORMAL);
		}
		
		// Initialisation des bateaux
		shipsToPlace = new ArrayList<Ship>(SHIPS_SIZES.length);
		ships = new ArrayList<Ship>(SHIPS_SIZES.length);
		for (int i = 0 ; i < SHIPS_SIZES.length ; i++) {
			shipsToPlace.add(new Ship(SHIPS_SIZES[i], epoch));
		}
	}
	
	/**
	 * Retourne la largeur de la grille.
	 * @return La largeur de la grille.
	 */
	public int getGridWidth() {
		return grid.length;
	}
	
	/**
	 * Retourne la hauteur de la grille.
	 * @return La hauteur de la grille.
	 */
	public int getGridHeight() {
		return grid[0].length;
	}
	
	/**
	 * Retourn l'ensemble de la taille des bateaux pr�sents sur la grille.
	 * @return Un tableau contenant l'ensemble de la taille des bateaux.
	 */
	public static int[] getShipsSizes() {
		return SHIPS_SIZES;
	}
	
	/**
	 * Retourne l'�tat de la case � une certaine position.
	 * @param x L'abscisse de la case.
	 * @param y L'ordonn�e de la case.
	 * @return L'�tat de la case.
	 */
	public SeaTileState getGridTileState(int x, int y) {
		return grid[x][y];
	}
	
	/**
	 * Retourne la liste des bateaux plac�s sur la grille.
	 * @return La liste des bateaux plac�s sur la grille.
	 */
	public List<Ship> getShips() {
		return ships;
	}
	
	/**
	 * Retourne la liste des bateaux � placer sur la grille.
	 * @return La liste des bateaux � placer sur la grille.
	 */
	public List<Ship> getShipsToPlace() {
		return shipsToPlace;
	}
	
	/**
	 * Retourne le bateau en cours de placement.
	 * @return Le bateau en cours de placement.
	 */
	public Ship getShipOnPlacing() {
		return shipOnPlacing;
	}
	
	/**
	 * Regarde si tous les bateaux sont plac�s sur la grille.
	 * @return Vrai si tous les bateaux sont plac�s sur la grille, faux sinon.
	 */
	public boolean areShipsAllPlaced() {
		return shipsToPlace.isEmpty() && shipOnPlacing == null;
	}
	
	/**
	 * Regarde si tous les bateaux de la grille sont d�truits.
	 * @return Vrai si tous les bateaux sont d�truits, faux sinon.
	 */
	public boolean areShipsAllDead() {
		if (!areShipsAllPlaced()) {	// S'il reste des bateaux � placer,
			return false;			// les bateaux ne peuvent pas �tre tous d�truits
		}
		
		boolean allDead = true;
		Iterator<Ship> iter = ships.iterator();
		while (iter.hasNext() && allDead) {	// On regarde si tous les bateaux sont d�truits
			Ship ship = iter.next();
			allDead = ship.isDead();
		}
		return allDead;
	}
	
	/**
	 * Regarde si la case � cette position est libre.
	 * Retourne faux si la case est hors des limites de la grille.
	 * @param position La position de la case.
	 * @return Vrai si la case � cette position est libre, faux sinon.
	 */
	public boolean isSeaTileFree(Position position) {
		if (position.isOutOfBounds(0, grid.length-1, 0, grid[0].length-1)) {
			return false;
		}
		
		boolean tileFree = true;
		Iterator<Ship> iter = ships.iterator();
		while (iter.hasNext() && tileFree) {
			Ship ship = iter.next();
			tileFree = !Arrays.asList(ship.getSeaTilesOccupied()).contains(position);
			// On v�rifie si la case est occup�e par le bateau
		}
		return tileFree;
	}
	
	/**
	 * Regarde si la position du bateau en cours de positionnement
	 * est valide.
	 * @return Vrai si la position du bateau en cours de positionnement est valide, faux sinon.
	 */
	public boolean isShipOnPlacingInValidPosition() {
		if (shipOnPlacing == null					// Si il n'y a pas de bateau en cours de positionnement
			|| shipOnPlacing.getPosition() == null	// ou que sa position n'est pas d�fini ou hors-limites,
			|| shipOnPlacing.getPosition().isOutOfBounds(0, grid.length-1, 0, grid[0].length-1)) {
			return false;							// le positionnement est invalide
		}
		
		Position[] tilesOccupied = shipOnPlacing.getSeaTilesOccupied();	// et on r�cup�re les cases qu'il occupe
		boolean validPlace = true;
		int i = 0;											// on regarde pour chaque case occup�e par le bateau
		while (i < tilesOccupied.length && validPlace) {
			validPlace = this.isSeaTileFree(tilesOccupied[i]);	// si elle est libre
			i++;
		}
		
		return validPlace;
	}
	
	/**
	 * Renvoie la liste des positions o� aucun tir n'a �t� effectu�.
	 * @return La liste des positions o� aucun tir n'a �t� effectu�.
	 */
	public List<Position> getAllNormalPositions() {
		List<Position> possibleShots = new ArrayList<Position>(grid.length * grid[0].length);
		for (int i = 0 ; i < grid.length ; i++) {
			for (int j = 0 ; j < grid[0].length ; j++) {
				if (grid[i][j] == SeaTileState.NORMAL) {
					possibleShots.add(new Position(i, j));
				}
			}
		}
		return possibleShots;
	}
	
	/**
	 * Prend un bateau de la liste des bateaux � placer et le met
	 * en tant que bateau en cours de positionnement s'il n'y en a pas d�j� un.
	 */
	public void putNextShipToPlace() {
		if (shipOnPlacing == null && !shipsToPlace.isEmpty()) {
			shipOnPlacing = shipsToPlace.remove(0);
		}
	}
	
	/**
	 * Valide le placement du bateau en cours de positionnement
	 * en l'ajoutant � la liste des bateaux actif
	 * et met le bateau suivant en cours de positionnement.
	 */
	public void validateShipPlacement() {
		if (shipOnPlacing != null) {
			ships.add(shipOnPlacing);
		}
		shipOnPlacing = null;
		putNextShipToPlace();
		setChanged();
		notifyObservers();
	}

	/**
	 * Fait le n�cessaire apr�s le tir d'un joueur.
	 * @param shotPos La position du tir.
	 * @return Vrai si le tir est valide, faux sinon.
	 */
	public boolean receiveShot(Position shotPos) {
		// Coordonn�es du tir non valide ou tir d�j� effectu� � cette position
		if (shotPos.isOutOfBounds(0, grid.length-1, 0, grid[0].length-1)
				|| grid[shotPos.getX()][shotPos.getY()] != SeaTileState.NORMAL) {
			return false;
		}
		
		boolean touched = false;
		Iterator<Ship> iter = ships.iterator();
		while (iter.hasNext() && !touched) {	// Pour chaque bateau,
			Ship ship = iter.next();
			touched = ship.checkShot(shotPos);	// on regarde si il est touch�
		}
		updateTileState(shotPos, touched);		// on met � jour l'�tat de la position du tir
		setChanged();
		notifyObservers();

		return true;							// on indique que le tir est valide
	}
	
	/**
	 * Met � jour l'�tat de la case de la grille selon si un bateau se trouve
	 * � cette position et donc est touch� ou non.
	 * @param position La position de la case.
	 * @param touched A vrai si un bateau est touch� � cette position, � faux sinon.
	 */
	private void updateTileState(Position position, boolean touched) {
		grid[position.getX()][position.getY()] = touched ? SeaTileState.TOUCHED : SeaTileState.SHOT; 
	}

}
