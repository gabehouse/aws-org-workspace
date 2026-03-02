// package com.gabe.animalia.ml.mapper;

// import com.gabe.animalia.ml.dtos.PlayerStateDTO;
// import com.gabe.animalia.ml.dtos.FighterStateDTO;
// import com.gabe.animalia.general.Player;
// import com.gabe.animalia.general.Critter;
// import java.util.List;
// import java.util.stream.Collectors;
// import java.util.ArrayList;
// import java.util.Arrays;

// /**
//  * Utility class to map the live Player Entity to the simplified PlayerStateDto.
//  * This class coordinates the transformation of the player's data and their fighters.
//  */
// public class PlayerMapper {

//     /**
//      * Maps a live Player object to a loggable DTO.
//      * * @param player The live Player entity from the game engine.
//      * @return A simplified PlayerStateDto for logging.
//      */
//     public static PlayerStateDTO toStateDto(Player player) {
//         if (player == null) {
//             return null;
//         }

//         // 1. Map the list of live Fighters to FighterStateDtos using CritterMapper
//         List<FighterStateDTO> fighterDtos = new ArrayList<>();
//         if (player.getCritters() != null) {
//             fighterDtos = Arrays.stream(player.getCritters())
//                 .map(CritterMapper::toFighterStateDTO)
//                 .collect(Collectors.toList());
//         }

//         // 2. Extract item names (assuming Player has a getItems() returning objects with names)
//         // If you don't have items yet, this can remain an empty list.
//         List<String> itemNames = new ArrayList<>();
//         /* if (player.getItems() != null) {
//             itemNames = player.getItems().stream()
//                 .map(item -> item.getName())
//                 .collect(Collectors.toList());
//         }
//         */

//         // 3. Construct and return the DTO
//         // Parameters: id, side, morale, usedTime, isBot, fighters, items
//         return new PlayerStateDTO(
//             player.getId(),
//             player.getMorale(),
//             fighterDtos,
//             itemNames
//         );
//     }
// }
