#!/usr/bin/php
<?php
    define('SOURCE', 'https://raw.github.com/essentials/Essentials/master/Essentials/src/items.csv');
    define('TARGET', 'items.txt');
    define('SKIPPED', 'skippeditems.txt');

    $target = @fopen(TARGET, 'wb');
    if (!$target)
    {
        echo "Failed to open " . TARGET . " for writing!\n";
        exit(1);
    }
    
    $skipped = @fopen(SKIPPED, 'wb');
    if (!$target)
    {
        echo "Failed to open " . SKIPPED . " for writing!\n";
        exit(1);
    }

    $lines = file(SOURCE);
    if (!$lines)
    {
        echo SOURCE . " could not be read!\n";
        exit(1);
    }
    
    $lastId = -1;
    $lastData = -1;
    $lastName = null;
    foreach ($lines as $line)
    {
        $line = trim($line);
        if (empty($line) || $line[0] == '#' || substr_count($line, ',') !== 2)
        {
            continue;
        }
        list($name, $id, $data) = explode(',', $line);
        
        if (!is_null($lastName) && levenshtein($lastName, $name) <= 3)
        {
            fwrite($skipped, "$name\n");
            continue;
        }
        $lastName = $name;
        
        if ($lastId == $id && $lastData == $data )
        {
            fwrite($target, "  $name\n");
        }
        else
        {
            fwrite($target, "$id:$data\n  $name\n");
            $lastId = $id;
            $lastData = $data;
            $lastName = null;
        }
    }
    fclose($target);
    fclose($skipped);
?>