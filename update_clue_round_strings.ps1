function Set-Or-InsertKey {
    param(
        [string]$Content,
        [string]$Key,
        [string]$Value,
        [string]$AfterKey
    )

    $pattern = '<string name="' + $Key + '">.*?</string>'
    $replacement = '<string name="' + $Key + '">' + $Value + '</string>'
    if ($Content -match $pattern) {
        return [regex]::Replace($Content, $pattern, $replacement, 1)
    }

    $anchorPattern = '<string name="' + $AfterKey + '">.*?</string>'
    if ($Content -match $anchorPattern) {
        return [regex]::Replace(
            $Content,
            $anchorPattern,
            { param($m) "$($m.Value)`r`n    $replacement" },
            1
        )
    }

    return $Content
}

$translations = @{
    'values' = @{
        setup_clue_rounds_title = 'Rounds per game'
        setup_clue_round_option_1 = '1 round'
        setup_clue_round_option_2 = '2 rounds'
        setup_clue_round_option_3 = '3 rounds'
        setup_clue_round_hint_1 = '1 round: fast game, less information.'
        setup_clue_round_hint_2 = '2 rounds: the most balanced option.'
        setup_clue_round_hint_3 = '3 rounds: more tension and deduction.'
        round_ready_instructions = 'Each player says one word per round. After the agreed rounds, vote for the impostor.'
        round_ready_clue_rounds = 'Rounds for this game: %1$d'
        round_ready_word_per_turn = 'Each player says one single word per round.'
        round_ready_vote_after_rounds = 'When you finish %1$d rounds, vote for the impostor.'
        how_preparation_line_1 = 'Choose player count, categories and rounds (1, 2 or 3).'
        how_play_line_2 = 'Each player says one single word related to the secret word.'
        how_play_line_3 = 'Do not change the number of rounds during the game.'
        how_win_line_1 = 'After the agreed rounds, vote for who you think is the impostor.'
        how_tip_2 = '2 rounds is usually the most balanced option.'
        error_clue_rounds = 'Rounds per game must be between 1 and 3.'
    }
    'values-es' = @{
        setup_clue_rounds_title = 'Vueltas por partida'
        setup_clue_round_option_1 = '1 vuelta'
        setup_clue_round_option_2 = '2 vueltas'
        setup_clue_round_option_3 = '3 vueltas'
        setup_clue_round_hint_1 = '1 vuelta: partida rapida, menos informacion.'
        setup_clue_round_hint_2 = '2 vueltas: la opcion mas equilibrada.'
        setup_clue_round_hint_3 = '3 vueltas: mas tension y mas deduccion.'
        round_ready_instructions = 'Cada jugador dice una sola palabra por vuelta. Al terminar las vueltas pactadas, votad al impostor.'
        round_ready_clue_rounds = 'Vueltas de esta partida: %1$d'
        round_ready_word_per_turn = 'Cada jugador dira una sola palabra por vuelta.'
        round_ready_vote_after_rounds = 'Cuando completeis las %1$d vueltas, votad al impostor.'
        how_preparation_line_1 = 'Elige jugadores, categorias y 1, 2 o 3 vueltas.'
        how_play_line_2 = 'Cada jugador dice una sola palabra relacionada con la palabra secreta.'
        how_play_line_3 = 'No cambieis el numero de vueltas a mitad de partida.'
        how_win_line_1 = 'Al terminar las vueltas pactadas, votad quien es el impostor.'
        how_tip_2 = '2 vueltas suele ser la opcion mas equilibrada.'
        error_clue_rounds = 'Las vueltas por partida deben estar entre 1 y 3.'
    }
    'values-de' = @{
        setup_clue_rounds_title = 'Runden pro Partie'
        setup_clue_round_option_1 = '1 Runde'
        setup_clue_round_option_2 = '2 Runden'
        setup_clue_round_option_3 = '3 Runden'
        setup_clue_round_hint_1 = '1 Runde: schnell, weniger Informationen.'
        setup_clue_round_hint_2 = '2 Runden: meist die ausgewogenste Option.'
        setup_clue_round_hint_3 = '3 Runden: mehr Spannung und Deduktion.'
        round_ready_instructions = 'Jede Person sagt pro Runde genau ein Wort. Nach den vereinbarten Runden stimmt ihr ab.'
        round_ready_clue_rounds = 'Runden in dieser Partie: %1$d'
        round_ready_word_per_turn = 'Jede Person sagt pro Runde genau ein Wort.'
        round_ready_vote_after_rounds = 'Wenn ihr %1$d Runden abgeschlossen habt, stimmt fuer den Betrueger ab.'
        how_preparation_line_1 = 'Waehle Spielerzahl, Kategorien und 1, 2 oder 3 Runden.'
        how_play_line_2 = 'Jede Person sagt pro Runde genau ein Wort zum geheimen Wort.'
        how_play_line_3 = 'Aendert die Rundenzahl nicht waehrend der Partie.'
        how_win_line_1 = 'Nach den vereinbarten Runden stimmt ihr ab, wer der Betrueger ist.'
        how_tip_2 = '2 Runden sind meist die ausgewogenste Wahl.'
        error_clue_rounds = 'Runden pro Partie muessen zwischen 1 und 3 liegen.'
    }
    'values-fr' = @{
        setup_clue_rounds_title = 'Tours par partie'
        setup_clue_round_option_1 = '1 tour'
        setup_clue_round_option_2 = '2 tours'
        setup_clue_round_option_3 = '3 tours'
        setup_clue_round_hint_1 = '1 tour : partie rapide, moins d informations.'
        setup_clue_round_hint_2 = '2 tours : option la plus equilibree.'
        setup_clue_round_hint_3 = '3 tours : plus de tension et deduction.'
        round_ready_instructions = 'Chaque joueur dit un seul mot par tour. Apres les tours convenus, votez imposteur.'
        round_ready_clue_rounds = 'Tours de cette partie : %1$d'
        round_ready_word_per_turn = 'Chaque joueur dira un seul mot par tour.'
        round_ready_vote_after_rounds = 'Quand vous avez fini les %1$d tours, votez pour imposteur.'
        how_preparation_line_1 = 'Choisissez joueurs, categories et 1, 2 ou 3 tours.'
        how_play_line_2 = 'Chaque joueur dit un seul mot lie au mot secret a chaque tour.'
        how_play_line_3 = 'Ne changez pas le nombre de tours pendant la partie.'
        how_win_line_1 = 'Apres les tours convenus, votez pour imposteur presume.'
        how_tip_2 = '2 tours est souvent l option la plus equilibree.'
        error_clue_rounds = 'Le nombre de tours doit etre entre 1 et 3.'
    }
    'values-it' = @{
        setup_clue_rounds_title = 'Giri per partita'
        setup_clue_round_option_1 = '1 giro'
        setup_clue_round_option_2 = '2 giri'
        setup_clue_round_option_3 = '3 giri'
        setup_clue_round_hint_1 = '1 giro: partita rapida, meno informazioni.'
        setup_clue_round_hint_2 = '2 giri: opzione piu equilibrata.'
        setup_clue_round_hint_3 = '3 giri: piu tensione e deduzione.'
        round_ready_instructions = 'Ogni giocatore dice una sola parola per giro. Dopo i giri concordati, votate l impostore.'
        round_ready_clue_rounds = 'Giri di questa partita: %1$d'
        round_ready_word_per_turn = 'Ogni giocatore dira una sola parola per giro.'
        round_ready_vote_after_rounds = 'Quando completate i %1$d giri, votate l impostore.'
        how_preparation_line_1 = 'Scegli giocatori, categorie e 1, 2 o 3 giri.'
        how_play_line_2 = 'Ogni giocatore dice una sola parola legata alla parola segreta per ogni giro.'
        how_play_line_3 = 'Non cambiate il numero di giri durante la partita.'
        how_win_line_1 = 'Dopo i giri concordati, votate chi pensate sia l impostore.'
        how_tip_2 = '2 giri e di solito la scelta piu equilibrata.'
        error_clue_rounds = 'I giri per partita devono essere tra 1 e 3.'
    }
    'values-pt' = @{
        setup_clue_rounds_title = 'Voltas por partida'
        setup_clue_round_option_1 = '1 volta'
        setup_clue_round_option_2 = '2 voltas'
        setup_clue_round_option_3 = '3 voltas'
        setup_clue_round_hint_1 = '1 volta: partida rapida, menos informacao.'
        setup_clue_round_hint_2 = '2 voltas: opcao mais equilibrada.'
        setup_clue_round_hint_3 = '3 voltas: mais tensao e deducao.'
        round_ready_instructions = 'Cada jogador diz uma palavra por volta. Ao terminar as voltas combinadas, votem no impostor.'
        round_ready_clue_rounds = 'Voltas desta partida: %1$d'
        round_ready_word_per_turn = 'Cada jogador dira uma unica palavra por volta.'
        round_ready_vote_after_rounds = 'Quando completarem as %1$d voltas, votem no impostor.'
        how_preparation_line_1 = 'Escolha jogadores, categorias e 1, 2 ou 3 voltas.'
        how_play_line_2 = 'Cada jogador diz uma unica palavra ligada a palavra secreta em cada volta.'
        how_play_line_3 = 'Nao mudem o numero de voltas durante a partida.'
        how_win_line_1 = 'Depois das voltas combinadas, votem em quem e o impostor.'
        how_tip_2 = '2 voltas costuma ser a opcao mais equilibrada.'
        error_clue_rounds = 'As voltas por partida devem estar entre 1 e 3.'
    }
    'values-ja' = @{
        setup_clue_rounds_title = '1ゲームの周回数'
        setup_clue_round_option_1 = '1周'
        setup_clue_round_option_2 = '2周'
        setup_clue_round_option_3 = '3周'
        setup_clue_round_hint_1 = '1周: 短時間で情報は少なめ。'
        setup_clue_round_hint_2 = '2周: いちばんバランスが良い設定。'
        setup_clue_round_hint_3 = '3周: 緊張感と推理が増える。'
        round_ready_instructions = '各周で全員が1語だけ言います。決めた周回が終わったら潜入者に投票します。'
        round_ready_clue_rounds = 'このゲームの周回数: %1$d'
        round_ready_word_per_turn = '各プレイヤーは1周ごとに1語だけ言います。'
        round_ready_vote_after_rounds = '%1$d周が終わったら潜入者に投票してください。'
        how_preparation_line_1 = '人数、カテゴリ、そして1/2/3周のどれかを決めます。'
        how_play_line_2 = '各プレイヤーは各周で秘密の単語に関連する1語だけを言います。'
        how_play_line_3 = 'ゲーム中に周回数は変更しません。'
        how_win_line_1 = '決めた周回が終わったら、誰が潜入者か投票します。'
        how_tip_2 = '2周が最もバランスのよい設定です。'
        error_clue_rounds = '1ゲームの周回数は1〜3の範囲で設定してください。'
    }
}

$roots = @('values','values-es','values-de','values-fr','values-it','values-pt','values-ja')
foreach ($root in $roots) {
    $path = "app/src/main/res/$root/strings.xml"
    $raw = Get-Content -Raw -Encoding UTF8 $path
    $t = $translations[$root]

    $raw = Set-Or-InsertKey $raw 'setup_clue_rounds_title' $t.setup_clue_rounds_title 'setup_impostor_count_item'
    $raw = Set-Or-InsertKey $raw 'setup_clue_round_option_1' $t.setup_clue_round_option_1 'setup_clue_rounds_title'
    $raw = Set-Or-InsertKey $raw 'setup_clue_round_option_2' $t.setup_clue_round_option_2 'setup_clue_round_option_1'
    $raw = Set-Or-InsertKey $raw 'setup_clue_round_option_3' $t.setup_clue_round_option_3 'setup_clue_round_option_2'
    $raw = Set-Or-InsertKey $raw 'setup_clue_round_hint_1' $t.setup_clue_round_hint_1 'setup_clue_round_option_3'
    $raw = Set-Or-InsertKey $raw 'setup_clue_round_hint_2' $t.setup_clue_round_hint_2 'setup_clue_round_hint_1'
    $raw = Set-Or-InsertKey $raw 'setup_clue_round_hint_3' $t.setup_clue_round_hint_3 'setup_clue_round_hint_2'

    $raw = Set-Or-InsertKey $raw 'round_ready_instructions' $t.round_ready_instructions 'round_ready_title'
    $raw = Set-Or-InsertKey $raw 'round_ready_clue_rounds' $t.round_ready_clue_rounds 'round_ready_instructions'
    $raw = Set-Or-InsertKey $raw 'round_ready_word_per_turn' $t.round_ready_word_per_turn 'round_ready_clue_rounds'
    $raw = Set-Or-InsertKey $raw 'round_ready_vote_after_rounds' $t.round_ready_vote_after_rounds 'round_ready_word_per_turn'

    $raw = Set-Or-InsertKey $raw 'how_preparation_line_1' $t.how_preparation_line_1 'how_preparation_title'
    $raw = Set-Or-InsertKey $raw 'how_play_line_2' $t.how_play_line_2 'how_play_line_1'
    $raw = Set-Or-InsertKey $raw 'how_play_line_3' $t.how_play_line_3 'how_play_line_2'
    $raw = Set-Or-InsertKey $raw 'how_win_line_1' $t.how_win_line_1 'how_win_title'
    $raw = Set-Or-InsertKey $raw 'how_tip_2' $t.how_tip_2 'how_tip_1'

    $raw = Set-Or-InsertKey $raw 'error_clue_rounds' $t.error_clue_rounds 'error_round_time'

    Set-Content -Encoding UTF8 $path $raw
}
