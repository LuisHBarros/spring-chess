package com.chess.social.domain.model;

import com.chess.social.domain.exception.DuplicateRankException;
import com.chess.social.domain.exception.RankNotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class GuildCategory {
    private final CategoryId id;
    private CategoryName name;
    private String description;
    private final List<GuildRank> ranks;

    private GuildCategory(CategoryId id, CategoryName name, String description, List<GuildRank> ranks) {
        if (id == null) {
            throw new IllegalArgumentException("CategoryId cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("CategoryName cannot be null");
        }
        this.id = id;
        this.name = name;
        this.description = description != null ? description : "";
        this.ranks = ranks != null ? new ArrayList<>(ranks) : new ArrayList<>();
    }

    public static GuildCategory create(CategoryName name, String description) {
        return new GuildCategory(CategoryId.generate(), name, description, new ArrayList<>());
    }

    public static GuildCategory reconstitute(CategoryId id, CategoryName name, String description, List<GuildRank> ranks) {
        return new GuildCategory(id, name, description, ranks);
    }

    public GuildRank addRank(RankName rankName, int priority, Set<RankPermission> permissions) {
        boolean exists = ranks.stream().anyMatch(r -> r.getName().equals(rankName));
        if (exists) {
            throw new DuplicateRankException("Rank with name '" + rankName.getValue() + "' already exists in category '" + name.getValue() + "'");
        }
        GuildRank newRank = GuildRank.create(rankName, priority, permissions);
        ranks.add(newRank);
        return newRank;
    }

    public void removeRank(RankId rankId) {
        GuildRank rank = findRank(rankId)
                .orElseThrow(() -> new RankNotFoundException("Rank with ID " + rankId + " not found in category '" + name.getValue() + "'"));
        ranks.remove(rank);
    }

    public Optional<GuildRank> findRank(RankId rankId) {
        return ranks.stream().filter(r -> r.getId().equals(rankId)).findFirst();
    }

    public Optional<GuildRank> findRankByName(RankName rankName) {
        return ranks.stream().filter(r -> r.getName().equals(rankName)).findFirst();
    }

    public void updateDetails(CategoryName newName, String newDescription) {
        if (newName == null) {
            throw new IllegalArgumentException("CategoryName cannot be null");
        }
        this.name = newName;
        this.description = newDescription != null ? newDescription : "";
    }

    public CategoryId getId() {
        return id;
    }

    public CategoryName getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<GuildRank> getRanks() {
        return Collections.unmodifiableList(ranks);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GuildCategory category = (GuildCategory) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
