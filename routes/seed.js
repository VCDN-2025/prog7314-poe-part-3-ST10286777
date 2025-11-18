// routes/seed.js - Create this new file

import express from "express";
const router = express.Router();
import { db } from '../config/firebase-admin.js';

const sampleQuestions = [
  {
    category: "Video Games",
    questionText: "What is the name of Mario’s brother in the Super Mario series?",
    choices: ["Luigi", "Wario", "Yoshi", "Toad"],
    answer: "Luigi",
    difficulty: "Easy"
  },
  {
    category: "Video Games",
    questionText: "Which company developed the game 'Fortnite'?",
    choices: ["Activision", "Epic Games", "Ubisoft", "EA"],
    answer: "Epic Games",
    difficulty: "Easy"
  },
  {
    category: "Video Games",
    questionText: "In 'Minecraft', which material do you need to mine obsidian?",
    choices: ["Iron Pickaxe", "Diamond Pickaxe", "Gold Pickaxe", "Stone Pickaxe"],
    answer: "Diamond Pickaxe",
    difficulty: "Medium"
  },
  {
    category: "Video Games",
    questionText: "What year was the first PlayStation console released?",
    choices: ["1994", "1996", "1998", "2000"],
    answer: "1994",
    difficulty: "Hard"
  },
  {
    category: "Video Games",
    questionText: "In 'The Legend of Zelda' series, what is the name of the main protagonist?",
    choices: ["Zelda", "Link", "Ganon", "Epona"],
    answer: "Link",
    difficulty: "Easy"
  },
  {
    category: "Music",
    questionText: "Who is known as the 'King of Pop'?",
    choices: ["Elvis Presley", "Michael Jackson", "Prince", "Freddie Mercury"],
    answer: "Michael Jackson",
    difficulty: "Easy"
  },
  {
    category: "Music",
    questionText: "Which instrument has 88 keys?",
    choices: ["Guitar", "Piano", "Violin", "Flute"],
    answer: "Piano",
    difficulty: "Easy"
  },
  {
    category: "Music",
    questionText: "Which British band released the album 'Abbey Road'?",
    choices: ["The Beatles", "Queen", "Oasis", "The Rolling Stones"],
    answer: "The Beatles",
    difficulty: "Medium"
  },
  {
    category: "Music",
    questionText: "What is the highest male singing voice?",
    choices: ["Tenor", "Baritone", "Bass", "Alto"],
    answer: "Tenor",
    difficulty: "Medium"
  },
  {
    category: "Music",
    questionText: "Which composer became deaf later in life but continued to compose music?",
    choices: ["Mozart", "Bach", "Beethoven", "Chopin"],
    answer: "Beethoven",
    difficulty: "Hard"
  },
  {
    category: "Science",
    questionText: "What is the chemical symbol for gold?",
    choices: ["Au", "Ag", "Fe", "Cu"],
    answer: "Au",
    difficulty: "Easy"
  },
  {
    category: "Science",
    questionText: "What planet is known as the Red Planet?",
    choices: ["Venus", "Mars", "Jupiter", "Mercury"],
    answer: "Mars",
    difficulty: "Easy"
  },
  {
    category: "Science",
    questionText: "What gas do plants absorb during photosynthesis?",
    choices: ["Oxygen", "Carbon Dioxide", "Nitrogen", "Hydrogen"],
    answer: "Carbon Dioxide",
    difficulty: "Medium"
  },
  {
    category: "Science",
    questionText: "How many bones are in the adult human body?",
    choices: ["206", "208", "210", "212"],
    answer: "206",
    difficulty: "Medium"
  },
  {
    category: "Science",
    questionText: "What is the speed of light in a vacuum?",
    choices: ["300,000 km/s", "150,000 km/s", "1,000 km/s", "30,000 km/s"],
    answer: "300,000 km/s",
    difficulty: "Hard"
  },
  {
    category: "History",
    questionText: "Who painted the Mona Lisa?",
    choices: ["Leonardo da Vinci", "Pablo Picasso", "Vincent van Gogh", "Michelangelo"],
    answer: "Leonardo da Vinci",
    difficulty: "Medium"
  },
  {
    category: "History",
    questionText: "In which year did World War II end?",
    choices: ["1943", "1944", "1945", "1946"],
    answer: "1945",
    difficulty: "Easy"
  },
  {
    category: "History",
    questionText: "Which ancient civilization built the pyramids?",
    choices: ["Romans", "Greeks", "Egyptians", "Persians"],
    answer: "Egyptians",
    difficulty: "Medium"
  },
  {
    category: "History",
    questionText: "Who was known as the 'Maid of Orléans'?",
    choices: ["Cleopatra", "Joan of Arc", "Queen Elizabeth I", "Marie Antoinette"],
    answer: "Joan of Arc",
    difficulty: "Medium"
  },
  {
    category: "History",
    questionText: "Which empire was ruled by Genghis Khan?",
    choices: ["Ottoman Empire", "Roman Empire", "Mongol Empire", "Persian Empire"],
    answer: "Mongol Empire",
    difficulty: "Hard"
  },
  {
    category: "Geography",
    questionText: "What is the capital of Japan?",
    choices: ["Tokyo", "Kyoto", "Osaka", "Seoul"],
    answer: "Tokyo",
    difficulty: "Easy"
  },
  {
    category: "Geography",
    questionText: "What is the largest continent in the world?",
    choices: ["Africa", "Asia", "Europe", "North America"],
    answer: "Asia",
    difficulty: "Easy"
  },
  {
    category: "Geography",
    questionText: "Which ocean is the largest?",
    choices: ["Atlantic Ocean", "Indian Ocean", "Pacific Ocean", "Arctic Ocean"],
    answer: "Pacific Ocean",
    difficulty: "Easy"
  },
  {
    category: "Geography",
    questionText: "What is the capital city of Australia?",
    choices: ["Sydney", "Melbourne", "Canberra", "Perth"],
    answer: "Canberra",
    difficulty: "Medium"
  },
  {
    category: "Geography",
    questionText: "Which river flows through Egypt?",
    choices: ["Amazon River", "Nile River", "Yangtze River", "Tigris River"],
    answer: "Nile River",
    difficulty: "Medium"
  },
  {
    category: "Geography",
    questionText: "Mount Kilimanjaro is located in which country?",
    choices: ["Kenya", "Tanzania", "Uganda", "Ethiopia"],
    answer: "Tanzania",
    difficulty: "Hard"
  }
];

router.post('/seed-questions', async (req, res) => {
  try {
    const batch = db.batch();
    
    sampleQuestions.forEach((question, index) => {
      const questionRef = db.collection('questions').doc();
      const questionId = questionRef.id;
      
      batch.set(questionRef, {
        questionId,
        ...question,
        createdAt: new Date()
      });
    });
    
    await batch.commit();
    
    res.json({
      success: true,
      message: `Successfully added ${sampleQuestions.length} sample questions`,
      categories: [...new Set(sampleQuestions.map(q => q.category))]
    });
    
  } catch (error) {
    console.error('Error seeding questions:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to seed questions'
    });
  }
});

export default router;