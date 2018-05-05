package dk.itu.mario.engine.level;

import java.util.Random;
import java.util.*;

//Make any new member variables and functions you deem necessary.
//Make new constructors if necessary
//You must implement mutate() and crossover()


enum GeneType {
	HILL(2,5),
	GAP(1,5),
	PLATFORM(1,4),
	COINS(1,5),
	GOOMBAS(1,5),
	KOOPAS(1,5);

	public final int min;
	public final int max;

	GeneType (int min, int max) {
		this.min = min;
		this.max = max;
	}

	private static final List<GeneType> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
	private static final int SIZE = VALUES.size();
	private static final Random RANDOM = new Random();

	public static GeneType random()  {
		return VALUES.get(RANDOM.nextInt(SIZE));
	}
}

class Gene {
	public GeneType type;
	public int param;

	public Gene(GeneType type, int param) {
		this.type = type;
		this.param = param;
	}

	public Gene(Gene parent) {
		this.type = parent.type;
		this.param = parent.param;
	}

	public static Gene makeRandom() {
		GeneType t = GeneType.random();
		Random r = new Random();
		return new Gene(t, r.nextInt(t.max-t.min) + t.min);
	}

	public void mutate() {
		Random rand = new Random();
		double n = rand.nextFloat();
		if (n < 0.1) {
			this.type = GeneType.random();
		}
		this.param = rand.nextInt(this.type.max - this.type.min) + this.type.min;
	}
}

public class MyDNA extends DNA
{

	public int numGenes = 0; //number of genes
	public Gene[] genes;
	// Return a new DNA that differs from this one in a small way.
	// Do not change this DNA by side effect; copy it, change the copy, and return the copy.
	public MyDNA mutate ()
	{
		MyDNA copy = new MyDNA();
		//YOUR CODE GOES BELOW HERE
		copy.genes = new Gene[this.genes.length];
		for (int i = 0; i < this.genes.length; i++) {
			copy.genes[i] = new Gene(this.genes[i]);
		}
		Random rand = new Random();
		copy.genes[rand.nextInt(copy.genes.length)].mutate();
		//YOUR CODE GOES ABOVE HERE
		return copy;
	}

	public static MyDNA makeRandom(int numGenes) {
		MyDNA d = new MyDNA();
		d.genes = new Gene[numGenes];
		d.numGenes = numGenes;
		for (int i = 0; i < numGenes; i++) {
			d.genes[i] = Gene.makeRandom();
		}
		return d;
	}

	// Do not change this DNA by side effect
	public ArrayList<MyDNA> crossover (MyDNA mate)
	{
		ArrayList<MyDNA> offspring = new ArrayList<MyDNA>();
		//YOUR CODE GOES BELOW HERE

		//YOUR CODE GOES ABOVE HERE
		return offspring;
	}

	// Optional, modify this function if you use a means of calculating fitness other than using the fitness member variable.
	// Return 0 if this object has the same fitness as other.
	// Return -1 if this object has lower fitness than other.
	// Return +1 if this objet has greater fitness than other.
	public int compareTo(MyDNA other)
	{
		int result = super.compareTo(other);
		//YOUR CODE GOES BELOW HERE
		if (this.getFitness() < other.getFitness()) {
			result =  -1;
		} else if (this.getFitness() == other.getFitness()) {
			result =  0;
		} else {
			result =  1;
		}
		//YOUR CODE GOES ABOVE HERE
		return result;
	}


	// For debugging purposes (optional)
	public String toString ()
	{
		String s = "";
		//YOUR CODE GOES BELOW HERE
		StringBuilder sb = new StringBuilder();
		for (Gene g : this.genes) {
			sb.append(g.type.toString());
			sb.append(g.param);
			sb.append(":");
		}
		s = sb.toString();
		//YOUR CODE GOES ABOVE HERE
		return s;
	}

	public void setNumGenes (int n)
	{
		this.numGenes = n;
	}

}

